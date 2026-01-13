package com.shixun.simpleimserver.controller;

import com.shixun.simpleimserver.common.result.Result;
import com.shixun.simpleimserver.common.utils.SecurityUtils;
import com.shixun.simpleimserver.entity.Message;
import com.shixun.simpleimserver.service.ChatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
@RequestMapping("/api/chat")
@Api(tags = "聊天消息接口")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/history")
    @ApiOperation("获取历史消息记录")
    public Result<List<Message>> getHistory(
            @RequestParam Long targetId,
            @RequestParam Integer sessionType) { // 1:单聊 2:群聊

        Long currentUserId = SecurityUtils.getUserId();
        List<Message> list = chatService.getHistoryMsg(currentUserId, targetId, sessionType);

        return Result.success(list);
    }
}