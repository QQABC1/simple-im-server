package com.shixun.simpleimserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shixun.simpleimserver.entity.Message;
import com.shixun.simpleimserver.model.ws.WSMsg;

import java.util.List;

public interface ChatService extends IService<Message> {
    /**
     * 保存聊天消息
     * @param wsMsg 前端传来的 WebSocket 消息包
     * @return 保存后的实体（带ID）
     */
    Message saveMessage(WSMsg wsMsg);

    /**
     * 获取历史漫游消息
     * @param currentUserId 当前登录用户
     * @param targetId 聊天对象ID (好友ID 或 群ID)
     * @param sessionType 会话类型 (1:单聊 2:群聊)
     */
    List<Message> getHistoryMsg(Long currentUserId, Long targetId, Integer sessionType);
}