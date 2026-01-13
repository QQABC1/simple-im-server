package com.shixun.simpleimserver;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.shixun.simpleimserver.netty.UserChannelMap;
import com.shixun.simpleimserver.netty.handler.IMWebSocketHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class StatusNotificationTest {

    @Autowired
    private IMWebSocketHandler imWebSocketHandler; // 注入被测试的 Handler

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 定义测试用的两个用户 ID
    private static final Long USER_A_ID = 1001L; // 观察者
    private static final Long USER_B_ID = 1002L; // 被观察者

    @BeforeEach
    void setup() {
        // 1. 初始化数据库：确保 A 和 B 是好友
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("TRUNCATE TABLE friendships");
        // 插入好友关系 (A <-> B)
        jdbcTemplate.update("INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 1)", USER_A_ID, USER_B_ID);
        jdbcTemplate.update("INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 1)", USER_B_ID, USER_A_ID);
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

        // 2. 清理内存 Map
        UserChannelMap.remove(UserChannelMap.get(USER_A_ID));
        UserChannelMap.remove(UserChannelMap.get(USER_B_ID));
    }

    @AfterEach
    void tearDown() {
        // 清理内存
        UserChannelMap.remove(UserChannelMap.get(USER_A_ID));
        UserChannelMap.remove(UserChannelMap.get(USER_B_ID));
    }

    @Test
    public void testOnlineNotification() {
        System.out.println("=== 测试：好友上线通知 ===");

        // 1. 模拟用户 A 先上线 (创建虚拟通道)
        EmbeddedChannel channelA = new EmbeddedChannel(imWebSocketHandler);
        // 手动将 A 放入内存 Map (模拟 A 已经登录)
        UserChannelMap.put(USER_A_ID, channelA);

        // 2. 模拟用户 B 进行登录认证
        EmbeddedChannel channelB = new EmbeddedChannel(imWebSocketHandler);
        // 构造认证消息 JSON
        String authJson = String.format("{\"type\": 5, \"data\": {\"content\": \"%d\"}}", USER_B_ID);

        // 模拟 Netty 收到消息 (writeInbound 相当于收到客户端数据)
        channelB.writeInbound(new TextWebSocketFrame(authJson));

        // 3. 验证：检查 A 的通道是否收到了 B 的上线通知
        // readOutbound 获取服务端写给客户端的数据
        TextWebSocketFrame notificationFrame = channelA.readOutbound();

        assertNotNull(notificationFrame, "用户 A 应该收到通知消息");

        String jsonStr = notificationFrame.text();
        System.out.println("用户 A 收到的通知: " + jsonStr);

        JSONObject json = JSON.parseObject(jsonStr);

        // 断言：类型应该是 6 (USER_STATUS)
        assertEquals(6, json.getIntValue("type"));
        // 断言：是谁变了？应该是 B (1002)
        assertEquals(USER_B_ID, json.getLong("senderId"));
        // 断言：变成了什么？应该是 "1" (上线)
        assertEquals("1", json.getJSONObject("data").getString("content"));
    }

    @Test
    public void testOfflineNotification() throws Exception {
        System.out.println("=== 测试：好友下线通知 ===");

        // 1. 模拟 A 和 B 都已在线
        EmbeddedChannel channelA = new EmbeddedChannel(imWebSocketHandler);
        UserChannelMap.put(USER_A_ID, channelA);

        EmbeddedChannel channelB = new EmbeddedChannel(imWebSocketHandler);
        UserChannelMap.put(USER_B_ID, channelB);
        // 关键：设置 Attribute
        channelB.attr(UserChannelMap.USER_ID_KEY).set(USER_B_ID);

        // 2. 模拟 B 断开连接
        // ✅ 这一步会自动触发 handler.channelInactive，并执行广播逻辑
        channelB.close();

        // ❌ 删除下面这行！不要手动调用，否则会报空指针
        // imWebSocketHandler.channelInactive(channelB.pipeline().context(imWebSocketHandler));

        // 3. 验证：检查 A 是否收到了下线通知
        TextWebSocketFrame notificationFrame = channelA.readOutbound();

        assertNotNull(notificationFrame, "用户 A 应该收到下线通知");

        String jsonStr = notificationFrame.text();
        System.out.println("用户 A 收到的通知: " + jsonStr);

        JSONObject json = JSON.parseObject(jsonStr);

        assertEquals(6, json.getIntValue("type"));
        assertEquals(USER_B_ID, json.getLong("senderId"));
        assertEquals("0", json.getJSONObject("data").getString("content"));
    }
}