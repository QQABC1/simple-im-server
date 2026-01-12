package com.shixun.simpleimserver.common.constant;

/**
 * WebSocket 消息类型定义
 */
public interface MsgType {
    int CHAT_TEXT = 1;   // 文本消息
    int CHAT_FILE = 2;   // 文件/图片
    int CHAT_NUDGE = 3;  // 窗口抖动
    int HEARTBEAT = 4;   // 心跳包
    int AUTH = 5;        // 认证消息 (连接后第一条)
}