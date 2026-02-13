package com.shixun.simpleimserver.netty.handler.strategy.impl;

import com.alibaba.fastjson2.JSON;
import com.shixun.simpleimserver.common.constant.MsgType;
import com.shixun.simpleimserver.model.ws.WSMsg;
import com.shixun.simpleimserver.netty.UserChannelMap;
import com.shixun.simpleimserver.netty.handler.strategy.MessageHandler;
import com.shixun.simpleimserver.service.ChatService;
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

    @Override
    public List<Integer> getSupportedTypes() {
        // 这个 Handler 同时处理文本、文件、抖动
        return Arrays.asList(MsgType.CHAT_TEXT, MsgType.CHAT_FILE, MsgType.CHAT_NUDGE);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, WSMsg msg) {
        Long receiverId = msg.getReceiverId();

        // 2. 持久化存储
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
}