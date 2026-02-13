package com.shixun.simpleimserver.netty.handler.strategy;

import com.shixun.simpleimserver.common.constant.MsgType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MessageHandlerFactory {

    private final Map<Integer, MessageHandler> handlerMap = new HashMap<>();

    /**
     * 构造函数注入：Spring 会自动把所有 MessageHandler 的实现类注入到 List 中
     */
    @Autowired
    public MessageHandlerFactory(List<MessageHandler> handlers) {
        for (MessageHandler handler : handlers) {
            List<Integer> types = handler.getSupportedTypes();
            if (types != null) {
                for (Integer type : types) {
                    handlerMap.put(type, handler);
                }
            }
        }
    }

    /**
     * 根据消息类型获取对应的处理器
     */
    public MessageHandler getHandler(Integer type) {
        return handlerMap.get(type);
    }
}