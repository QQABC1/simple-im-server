package com.shixun.simpleimserver.model.ws;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WSMsg {
    private Integer type;        // 消息类型 (参考 MsgType)
    private Long senderId;       // 发送者ID
    private Long receiverId;     // 接收者ID (好友ID 或 群ID)
    private Integer sessionType; // 1:单聊 2:群聊
    private WSData data;         // 消息载体
    private LocalDateTime sendTime; // 发送时间
}