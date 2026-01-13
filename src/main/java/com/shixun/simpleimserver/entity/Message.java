package com.shixun.simpleimserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("messages")
public class Message {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发送者ID */
    private Long fromId;

    /** 接收者ID (好友ID 或 群ID) */
    private Long toId;

    /** 会话类型 1:单聊 2:群聊 */
    private Integer sessionType;

    /** 消息类型 1:纯文本 2:富文本(带字体) 3:文件/图片 4:窗口抖动 */
    private Integer msgType;

    /** 消息内容/文件地址/JSON */
    private String content;

    /** 发送时间 */
    private LocalDateTime sendTime;

    /** 状态 0:未读 1:已读 2:撤回 */
    private Integer status;
}