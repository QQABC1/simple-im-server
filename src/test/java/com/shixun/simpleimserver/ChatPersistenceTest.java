package com.shixun.simpleimserver;

import com.shixun.simpleimserver.entity.Message;
import com.shixun.simpleimserver.model.ws.FontStyle;
import com.shixun.simpleimserver.model.ws.WSData;
import com.shixun.simpleimserver.model.ws.WSMsg;
import com.shixun.simpleimserver.service.ChatService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ChatPersistenceTest {

    @Autowired
    private ChatService chatService;

    @Test
    public void testSaveMessage() {
        // 1. 构造模拟的前端消息包
        WSMsg wsMsg = new WSMsg();
        wsMsg.setType(1); // CHAT_TEXT
        wsMsg.setSenderId(888L);
        wsMsg.setReceiverId(999L);
        wsMsg.setSessionType(1); // 单聊

        WSData data = new WSData();
        data.setContent("JUnit 单元测试消息");

        // 模拟带字体
        FontStyle font = new FontStyle();
        font.setColor("#000000");
        font.setSize(16);
        data.setFont(font);

        wsMsg.setData(data);

        // 2. 调用 Service 保存
        Message savedMsg = chatService.saveMessage(wsMsg);

        // 3. 断言验证
        Assertions.assertNotNull(savedMsg.getId(), "保存失败，ID为空");
        Assertions.assertEquals(888L, savedMsg.getFromId());

        System.out.println(">>> 消息保存成功，ID: " + savedMsg.getId());
        System.out.println(">>> 存储内容: " + savedMsg.getContent());
    }
}