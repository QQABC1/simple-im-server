package com.shixun.simpleimserver.netty.handler.strategy.impl;
import com.alibaba.fastjson2.JSON;
import com.shixun.simpleimserver.common.constant.MsgType;
import com.shixun.simpleimserver.model.ws.WSData;
import com.shixun.simpleimserver.model.ws.WSMsg;
import com.shixun.simpleimserver.netty.UserChannelMap;
import com.shixun.simpleimserver.netty.handler.strategy.MessageHandler;
import com.shixun.simpleimserver.service.NotifyService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;


@Component
public class AuthHandler implements MessageHandler {

    @Autowired
    private NotifyService notifyService;

    @Override
    public void handle(ChannelHandlerContext ctx, WSMsg msg) {
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
            ctx.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(response)));
            // 新增上线逻辑：广播上线通知 ("1")
            notifyService.notifyFriendStatusAsync(userId, "1");

        } catch (Exception e) {
            System.out.println("认证失败");
            ctx.close(); // 踢下线
        }
    }

    @Override
    public List<Integer> getSupportedTypes() {
        return Collections.singletonList(MsgType.AUTH);
    }

}