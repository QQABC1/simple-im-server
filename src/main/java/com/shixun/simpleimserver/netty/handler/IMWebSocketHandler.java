package com.shixun.simpleimserver.netty.handler;

import com.alibaba.fastjson2.JSON;
import com.shixun.simpleimserver.common.constant.MsgType;
import com.shixun.simpleimserver.common.utils.JwtUtil; // 确保你有 JwtUtil
import com.shixun.simpleimserver.model.ws.WSData;
import com.shixun.simpleimserver.model.ws.WSMsg;
import com.shixun.simpleimserver.netty.UserChannelMap;
import com.shixun.simpleimserver.service.ChatService;
import com.shixun.simpleimserver.service.FriendService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@ChannelHandler.Sharable // 标记该 Handler 可被多个 Channel 共享
public class IMWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    @Autowired
    private ChatService chatService;
    @Autowired
    private FriendService friendService;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("新的连接接入: " + ctx.channel().id());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 修改下线逻辑：先获取 ID 通知好友，再移除
        // 从 Channel 属性中获取绑定的 UserId (前提：UserChannelMap.put 时存入了 Attribute)
        Long userId = ctx.channel().attr(UserChannelMap.USER_ID_KEY).get();

        UserChannelMap.remove(ctx.channel());

        if (userId != null) {
            System.out.println("用户下线: " + userId);
            // 广播下线通知 ("0")
            notifyFriendStatus(userId, "0");
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        String text = frame.text();
        System.out.println("收到消息: " + text);

        try {
            // 1. 解析 JSON
            WSMsg msg = JSON.parseObject(text, WSMsg.class);
            if (msg == null || msg.getType() == null) return;

            // 2. 填充服务器时间
            msg.setSendTime(LocalDateTime.now());

            // 3. 根据消息类型分发逻辑
            switch (msg.getType()) {
                case MsgType.AUTH: // 认证
                    handleAuth(ctx, msg);
                    break;
                case MsgType.CHAT_TEXT:  // 文本
                case MsgType.CHAT_FILE:  // 文件
                case MsgType.CHAT_NUDGE: // 抖动
                    handleChat(ctx, msg);
                    break;
                case MsgType.HEARTBEAT: // 心跳
                    // 原样返回，或者回复 "pong"
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(text));
                    break;
                default:
                    System.out.println("未知消息类型");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("消息处理异常: " + e.getMessage());
        }
    }

    /**
     * 处理认证逻辑 (前端连接后发送的第一条消息)
     */
    private void handleAuth(ChannelHandlerContext ctx, WSMsg msg) {
        String token = msg.getData().getContent(); // 约定：Auth类型的content放token
        try {
            // TODO 解析 Token 获取 UserId (这里假设 JwtUtil 已经实现)
            // String userIdStr = JwtUtil.getUserIdFromToken(token);
            // 简单模拟: 假设 content 直接就是 userId (实际开发必须校验 Token!)
            Long userId = Long.parseLong(token);

            // 绑定连接
            UserChannelMap.put(userId, ctx.channel());
            System.out.println("用户认证成功, ID: " + userId);

            // 回复前端：认证成功
            WSMsg response = new WSMsg();
            response.setType(MsgType.CHAT_TEXT); // 借用文本消息类型，或者定义一个新的 SYS_MSG

            WSData data = new WSData();
            data.setContent("系统: 认证成功"); // 内容放这里
            response.setData(data);

            // 发送 JSON 字符串
            sendJson(ctx.channel(), JSON.toJSONString(response));

            // 新增上线逻辑：广播上线通知 ("1")
            notifyFriendStatus(userId, "1");

        } catch (Exception e) {
            System.out.println("认证失败");
            ctx.close(); // 踢下线
        }
    }

    /**
     * 处理聊天逻辑 (单聊)
     */
    private void handleChat(ChannelHandlerContext ctx, WSMsg msg) {
        Long receiverId = msg.getReceiverId();

        // ==========================================
        // 2. 【核心修改】持久化存储
        // ==========================================
        try {
            chatService.saveMessage(msg);
            // 可以在控制台打印一下，方便调试
            System.out.println("消息已保存至数据库, From:" + msg.getSenderId() + " To:" + receiverId);
        } catch (Exception e) {
            e.printStackTrace();
            // 实际生产中可能需要回执告知前端发送失败
        }

        // 3. 查找接收方 Channel 并转发 (原有逻辑)
        Channel toChannel = UserChannelMap.get(receiverId);

        if (toChannel != null && toChannel.isActive()) {
            String jsonPush = JSON.toJSONString(msg);
            toChannel.writeAndFlush(new TextWebSocketFrame(jsonPush));
            System.out.println("消息已转发给用户: " + receiverId);
        } else {
            System.out.println("用户 " + receiverId + " 不在线，消息已存库 (等上线拉取历史)");
        }
    }

    // 辅助方法：发送 JSON
    private void sendJson(Channel channel, String msg) {
        channel.writeAndFlush(new TextWebSocketFrame(msg));
    }

    /**
     *  新增辅助方法：通知所有在线好友状态变更
     * @param userId 当前发生变化的用户ID
     * @param status "1":上线, "0":下线
     */
    private void notifyFriendStatus(Long userId, String status) {
        // TODO 优化思路异步执行，防止阻塞 Netty I/O 线程
        // 如果没有线程池，暂时直接跑也没大问题，因为只是简单的内存操作
        try {
            // 1. 查出所有好友的 ID
            List<Long> friendIds = friendService.getFriendIdList(userId);

            if (friendIds == null || friendIds.isEmpty()) return;

            // 2. 构建通知消息
            WSMsg notifyMsg = new WSMsg();
            notifyMsg.setType(MsgType.USER_STATUS); // Type = 6
            notifyMsg.setSenderId(userId);          // 谁的状态变了
            WSData data = new WSData();
            data.setContent(status);                // 变成了什么状态
            notifyMsg.setData(data);

            String jsonStr = JSON.toJSONString(notifyMsg);

            // 3. 遍历好友，如果在线就推送
            for (Long friendId : friendIds) {
                Channel friendChannel = UserChannelMap.get(friendId);
                if (friendChannel != null && friendChannel.isActive()) {
                    friendChannel.writeAndFlush(new TextWebSocketFrame(jsonStr));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}