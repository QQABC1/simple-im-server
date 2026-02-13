package com.shixun.simpleimserver.netty.handler.strategy.impl;

import com.alibaba.fastjson2.JSON;
import com.shixun.simpleimserver.common.constant.MsgType;
import com.shixun.simpleimserver.model.ws.WSMsg;
import com.shixun.simpleimserver.netty.handler.strategy.MessageHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

@Component
public class HeartbeatHandler implements MessageHandler {

    @Override
    public List<Integer> getSupportedTypes() {
        return Collections.singletonList(MsgType.HEARTBEAT);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, WSMsg msg) {
        // 原样返回心跳，或者回写 pong
        ctx.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(msg)));
    }
}