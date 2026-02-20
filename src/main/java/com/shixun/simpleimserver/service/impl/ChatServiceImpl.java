package com.shixun.simpleimserver.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shixun.simpleimserver.common.constant.MsgType;
import com.shixun.simpleimserver.entity.Message;
import com.shixun.simpleimserver.mapper.MessageMapper;
import com.shixun.simpleimserver.model.ws.WSData;
import com.shixun.simpleimserver.model.ws.WSMsg;
import com.shixun.simpleimserver.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ChatServiceImpl extends ServiceImpl<MessageMapper, Message> implements ChatService {
    @Autowired
    private MessageMapper messageMapper;

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
        // TODO 实际生产中应使用分页 (lastId 游标分页)，这里 MVP 用 limit
        wrapper.orderByAsc(Message::getSendTime).last("LIMIT 100");

        return this.list(wrapper);
    }

    @Override
    public void updateStatus(String msgId, Integer status) {
        // 1. 参数校验
        if (msgId == null) return;

        // 2. 使用 LambdaUpdateWrapper 进行局部更新
        // 生成 SQL: UPDATE message SET status = ? WHERE id = ?
        this.update(new LambdaUpdateWrapper<Message>()
                .eq(Message::getId, Long.parseLong(msgId)) // 假设 DB ID 是 Long 类型
                .set(Message::getStatus, status)           // 只更新 status 字段
        );

        // 日志 (可选)
        System.out.println(">>> 异步更新消息状态完成: " + msgId);
    }

    /**
     * 拉取未读消息
     */
    public List<WSMsg> pullUnreadMessages(Long userId) {
        // 1. 查询 DB
        List<Message> entities = lambdaQuery()
                .eq(Message::getToId, userId)    // 接收者是当前用户
                .eq(Message::getStatus, 0)       // 未读状态
                .orderByDesc(Message::getSendTime) // 按发送时间倒序（可根据需求调整）
                .list();

        if (entities.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 实体转换 (Entity -> WSMsg)
        List<WSMsg> result = new ArrayList<>();
        List<Long> ids = new ArrayList<>();

        for (Message entity : entities) {
            WSMsg dto = new WSMsg();
            dto.setId(entity.getId().toString()); // 消息ID
            dto.setSenderId(entity.getFromId());
            dto.setReceiverId(entity.getToId());
            dto.setType(entity.getMsgType()); // 保持类型 (文本/图片/抖动)
            dto.setSendTime(entity.getSendTime());

            WSData data = new WSData();
            data.setContent(entity.getContent());
            dto.setData(data);

            result.add(dto);
            ids.add(entity.getId());
        }

        // 3. 【优化】拉取后，可以立刻将这些消息标记为 "已送达" (Delivered) 或 "已读"
        // 也可以等前端发回执再改，这里为了简单，拉取即视为送达
        if (!ids.isEmpty()) {
            // 异步执行更新，不阻塞当前的拉取返回
            CompletableFuture.runAsync(() -> {
                // 使用 MyBatis-Plus lambda 更新，将状态改为已读（假设已读状态为 1）
                lambdaUpdate()
                        .in(Message::getId, ids)
                        .set(Message::getStatus, 1) // 1 表示已读，根据实际字段值调整
                        .update();
            });
        }

        return result;
    }



}