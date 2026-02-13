package com.shixun.simpleimserver.netty.handler.strategy;

import com.shixun.simpleimserver.model.ws.WSMsg;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;

public interface MessageHandler {
    /**
     * 执行业务逻辑
     */
    void handle(ChannelHandlerContext ctx, WSMsg msg);

    /**
     * 该处理器支持的消息类型列表
     * 例如 ChatHandler 可能支持: [TEXT, FILE, NUDGE]
     */
    List<Integer> getSupportedTypes();
}