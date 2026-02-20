package com.shixun.simpleimserver.netty.handler.strategy.impl;

import com.alibaba.fastjson2.JSON;
import com.shixun.simpleimserver.common.constant.MsgType;
import com.shixun.simpleimserver.model.ws.WSMsg;
import com.shixun.simpleimserver.netty.UserChannelMap;
import com.shixun.simpleimserver.netty.handler.strategy.MessageHandler;
import com.shixun.simpleimserver.service.ChatService;
import com.shixun.simpleimserver.service.mq.MessageProducer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ChatHandler implements MessageHandler {

    @Autowired
    private ChatService chatService;

    @Autowired
    private MessageProducer messageProducer;

    @Override
    public List<Integer> getSupportedTypes() {
        // 这个 Handler 同时处理文本、文件、抖动
        return Arrays.asList(MsgType.CHAT_TEXT, MsgType.CHAT_FILE, MsgType.CHAT_NUDGE);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, WSMsg msg) {
        Long receiverId = msg.getReceiverId();

        // 1. 简单的参数校验
        if (msg.getReceiverId() == null) return;

        // 2. 【核心】直接丢给 MQ，任务结束
        // 耗时从几十毫秒降低到 1-2 毫秒
        try {
            messageProducer.sendChatMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: 如果 MQ 挂了，这里需要降级处理（比如记录到本地日志文件，或返回错误给前端）
        }
    }
}