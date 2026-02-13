package com.shixun.simpleimserver;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.shixun.simpleimserver.netty.UserChannelMap;
import com.shixun.simpleimserver.service.FriendService;
import com.shixun.simpleimserver.service.NotifyService;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotifyServiceTest {

    // 1. 模拟依赖的 FriendService
    @Mock
    private FriendService friendService;

    // 2. 注入被测试的 NotifyService
    @InjectMocks
    private NotifyService notifyService;

    /**
     * 测试场景：好友在线，成功推送消息
     * 难点：Mock 静态方法 UserChannelMap.get()
     */
    @Test
    void testNotifyFriendStatusAsync_Success() {
        // --- Given (准备数据) ---
        Long currentUserId = 1001L;
        Long friendId = 2002L;
        String status = "1"; // 上线

        // 1. 模拟 friendService 返回一个好友ID
        when(friendService.getFriendIdList(currentUserId))
                .thenReturn(Arrays.asList(friendId));

        // 2. 模拟 Netty 的 Channel (这是一个接口，直接 mock)
        Channel mockChannel = mock(Channel.class);
        // 模拟 channel 是活跃的 (isActive 返回 true)
        when(mockChannel.isActive()).thenReturn(true);

        // 3. 【关键】Mock 静态类 UserChannelMap
        // try-with-resources 写法，保证测试跑完自动关闭 Mock，否则会影响其他测试
        try (MockedStatic<UserChannelMap> userChannelMapMock = Mockito.mockStatic(UserChannelMap.class)) {

            // 告诉静态方法：当调用 UserChannelMap.get(2002L) 时，返回上面造的 mockChannel
            userChannelMapMock.when(() -> UserChannelMap.get(friendId))
                    .thenReturn(mockChannel);

            // --- When (执行测试) ---
            // 注意：虽然方法上有 @Async，但在单元测试(InjectMocks)中，不启动 Spring 容器，
            // 它就是个普通同步方法，直接运行，不需要 Thread.sleep 等待。
            notifyService.notifyFriendStatusAsync(currentUserId, status);

            // --- Then (验证结果) ---

            // 1. 验证 friendService 确实被调用了
            verify(friendService, times(1)).getFriendIdList(currentUserId);

            // 2. 验证消息是否真的发送给了 Channel
            // 使用 ArgumentCaptor 捕获发送的具体参数，验证消息内容是否正确
            ArgumentCaptor<TextWebSocketFrame> frameCaptor = ArgumentCaptor.forClass(TextWebSocketFrame.class);

            verify(mockChannel, times(1)).writeAndFlush(frameCaptor.capture());

            // 3. 校验发送的 JSON 内容
            TextWebSocketFrame sentFrame = frameCaptor.getValue();
            String jsonStr = sentFrame.text();

            // 解析 JSON 验证字段 (这里演示简单的字符串包含验证，严谨点可以用 JSONObject 解析)
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            assertEquals(6, jsonObject.getInteger("type")); // MsgType.USER_STATUS
            assertEquals(currentUserId, jsonObject.getLong("senderId"));
            assertEquals(status, jsonObject.getJSONObject("data").getString("content"));
        }
    }

    /**
     * 测试场景：好友列表为空，直接返回，不应该查询 Channel
     */
    @Test
    void testNotifyFriendStatusAsync_NoFriends() {
        // Given
        Long userId = 1001L;
        when(friendService.getFriendIdList(userId)).thenReturn(Collections.emptyList());

        // When
        notifyService.notifyFriendStatusAsync(userId, "1");

        // Then
        // 验证没有去调静态方法（不需要 mockStatic，因为逻辑在这里之前就 return 了）
        // 也可以验证 friendService 被调了
        verify(friendService).getFriendIdList(userId);
    }

    /**
     * 测试场景：好友不在线 (Channel 为 null 或 inactive)
     */
    @Test
    void testNotifyFriendStatusAsync_FriendOffline() {
        Long userId = 1001L;
        Long friendId = 2002L;

        when(friendService.getFriendIdList(userId)).thenReturn(Arrays.asList(friendId));

        // Mock 静态方法
        try (MockedStatic<UserChannelMap> userChannelMapMock = Mockito.mockStatic(UserChannelMap.class)) {
            // 模拟取不到 Channel (返回 null)
            userChannelMapMock.when(() -> UserChannelMap.get(friendId)).thenReturn(null);

            // When
            notifyService.notifyFriendStatusAsync(userId, "1");

            // Then
            // 既然 Channel 是 null，就不会有任何对象调用 writeAndFlush
            // 这里没法 verify channel，因为 channel 根本没创建出来
        }
    }
}