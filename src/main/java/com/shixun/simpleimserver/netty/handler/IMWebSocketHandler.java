package com.shixun.simpleimserver.netty.handler;

import com.alibaba.fastjson2.JSON;
import com.shixun.simpleimserver.common.constant.MsgType;
import com.shixun.simpleimserver.common.utils.JwtUtil; // 确保你有 JwtUtil
import com.shixun.simpleimserver.model.ws.WSData;
import com.shixun.simpleimserver.model.ws.WSMsg;
import com.shixun.simpleimserver.netty.UserChannelMap;
import com.shixun.simpleimserver.netty.handler.strategy.MessageHandler;
import com.shixun.simpleimserver.netty.handler.strategy.MessageHandlerFactory;

import com.shixun.simpleimserver.service.NotifyService;
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
    private MessageHandlerFactory messageHandlerFactory;

    @Autowired
    private NotifyService notifyService;

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
            notifyService.notifyFriendStatusAsync(userId, "0");
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        try {
            // 1. 解析
            String text = frame.text();
            WSMsg msg = JSON.parseObject(text, WSMsg.class);
            if (msg == null || msg.getType() == null) return;

            // 2. 预处理 (填充时间)
            msg.setSendTime(LocalDateTime.now());

            // 3. 【核心优化】使用工厂获取策略，消灭 switch-case
            MessageHandler handler = messageHandlerFactory.getHandler(msg.getType());

            if (handler != null) {
                handler.handle(ctx, msg);
            } else {
                System.out.println("未找到对应的消息处理器: Type=" + msg.getType());
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("消息处理异常: " + e.getMessage());
        }
    }



}