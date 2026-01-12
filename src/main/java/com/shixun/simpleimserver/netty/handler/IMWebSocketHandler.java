package com.shixun.simpleimserver.netty.handler;

import com.alibaba.fastjson2.JSON;
import com.shixun.simpleimserver.common.constant.MsgType;
import com.shixun.simpleimserver.common.utils.JwtUtil; // 确保你有 JwtUtil
import com.shixun.simpleimserver.model.ws.WSData;
import com.shixun.simpleimserver.model.ws.WSMsg;
import com.shixun.simpleimserver.netty.UserChannelMap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@ChannelHandler.Sharable // 标记该 Handler 可被多个 Channel 共享
public class IMWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("新的连接接入: " + ctx.channel().id());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 断开连接时，自动移除用户
        UserChannelMap.remove(ctx.channel());
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

        // 1. 查找接收方 Channel
        Channel toChannel = UserChannelMap.get(receiverId);

        // 2. 如果在线，直接转发
        if (toChannel != null && toChannel.isActive()) {
            // 将对象转回 JSON 字符串发送
            String jsonPush = JSON.toJSONString(msg);
            toChannel.writeAndFlush(new TextWebSocketFrame(jsonPush));
            System.out.println("消息已转发给用户: " + receiverId);
        } else {
            System.out.println("用户 " + receiverId + " 不在线，应存入离线消息库 (TODO)");
            // TODO: 调用 MessageService 保存到 MySQL
        }
    }

    // 辅助方法：发送 JSON
    private void sendJson(Channel channel, String msg) {
        channel.writeAndFlush(new TextWebSocketFrame(msg));
    }
}