package com.shixun.simpleimserver.netty;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.concurrent.ConcurrentHashMap;

public class UserChannelMap {
    // Key: userId, Value: Channel
    private static final ConcurrentHashMap<Long, Channel> userChannelMap = new ConcurrentHashMap<>();

    // 定义 Channel 内部属性 Key，用于在 Channel 中保存 userId，方便断线时移除
    public static final AttributeKey<Long> USER_ID_KEY = AttributeKey.valueOf("userId");

    /**
     * 绑定用户和通道
     */
    public static void put(Long userId, Channel channel) {
        userChannelMap.put(userId, channel);
        // 将 userId 存入 Channel 的属性中，类似 HttpSession.setAttribute
        channel.attr(USER_ID_KEY).set(userId);
    }

    /**
     * 移除通道
     */
    public static void remove(Channel channel) {
        if (channel == null) {
            return;
        }

        Long userId = channel.attr(USER_ID_KEY).get();
        if (userId != null) {
            userChannelMap.remove(userId);
            System.out.println("用户下线, ID: " + userId);
        }
    }

    /**
     * 获取通道
     */
    public static Channel get(Long userId) {
        return userChannelMap.get(userId);
    }

    /**
     * 检查是否在线
     */
    public static boolean isOnline(Long userId) {
        return userChannelMap.containsKey(userId);
    }

    /**
     * 打印当前在线人数 (调试用)
     */
    public static void printOnlineUsers() {
        System.out.println("当前在线用户数: " + userChannelMap.size());
    }
}