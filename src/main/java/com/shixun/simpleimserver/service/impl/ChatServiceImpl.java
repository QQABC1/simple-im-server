package com.shixun.simpleimserver.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shixun.simpleimserver.common.constant.MsgType;
import com.shixun.simpleimserver.entity.Message;
import com.shixun.simpleimserver.mapper.MessageMapper;
import com.shixun.simpleimserver.model.ws.WSData;
import com.shixun.simpleimserver.model.ws.WSMsg;
import com.shixun.simpleimserver.service.ChatService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatServiceImpl extends ServiceImpl<MessageMapper, Message> implements ChatService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Message saveMessage(WSMsg wsMsg) {
        Message message = new Message();

        // 1. 基础信息映射
        message.setFromId(wsMsg.getSenderId());
        message.setToId(wsMsg.getReceiverId());
        message.setSessionType(wsMsg.getSessionType()); // 1:单聊 2:群聊
        message.setSendTime(LocalDateTime.now());
        message.setStatus(0); // 默认未读

        // 2. 处理消息类型和内容
        // 数据库定义: 1:纯文本 2:富文本 3:文件 4:抖动
        // WS定义 (MsgType): 1:CHAT_TEXT, 2:CHAT_FILE, 3:CHAT_NUDGE

        int wsType = wsMsg.getType();
        WSData data = wsMsg.getData();

        if (wsType == MsgType.CHAT_TEXT) {
            // 判断是否包含字体设置，如果有字体则存为富文本(Type=2)
            if (data.getFont() != null) {
                message.setMsgType(2); // 富文本
                // 将整个 data 对象转 JSON 存入 content，以便前端还原字体
                message.setContent(JSON.toJSONString(data));
            } else {
                message.setMsgType(1); // 纯文本
                message.setContent(data.getContent());
            }
        } else if (wsType == MsgType.CHAT_FILE) {
            message.setMsgType(3); // 文件
            message.setContent(data.getUrl()); // 存 URL
        } else if (wsType == MsgType.CHAT_NUDGE) {
            message.setMsgType(4); // 抖动
            message.setContent("窗口抖动");
        } else {
            // 其他类型默认处理
            message.setMsgType(1);
            message.setContent(JSON.toJSONString(data));
        }

        // 3. 执行插入
        this.save(message);

        return message;
    }
    /**
     * 获取历史漫游消息
     * @param currentUserId 当前登录用户
     * @param targetId 聊天对象ID (好友ID 或 群ID)
     * @param sessionType 会话类型 (1:单聊 2:群聊)
     */
    @Override
    public List<Message> getHistoryMsg(Long currentUserId, Long targetId, Integer sessionType) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();

        if (sessionType == 1) {
            // === 单聊逻辑 ===
            // 筛选条件：(from=我 AND to=他) OR (from=他 AND to=我)
            // 并且 session_type = 1
            wrapper.eq(Message::getSessionType, 1)
                    .and(w -> w
                            .nested(i -> i.eq(Message::getFromId, currentUserId).eq(Message::getToId, targetId))
                            .or()
                            .nested(i -> i.eq(Message::getFromId, targetId).eq(Message::getToId, currentUserId))
                    );
        } else {
            // === 群聊逻辑 (简单版) ===
            // 筛选条件：to_id = 群ID 并且 session_type = 2
            wrapper.eq(Message::getSessionType, 2)
                    .eq(Message::getToId, targetId);
        }

        // 按时间正序排列，限制最近 100 条
        // 实际生产中应使用分页 (lastId 游标分页)，这里 MVP 用 limit
        wrapper.orderByAsc(Message::getSendTime).last("LIMIT 100");

        return this.list(wrapper);
    }

}