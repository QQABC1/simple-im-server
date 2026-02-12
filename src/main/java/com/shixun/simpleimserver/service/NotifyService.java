package com.shixun.simpleimserver.service;

import com.alibaba.fastjson2.JSON;
import com.shixun.simpleimserver.common.constant.MsgType;
import com.shixun.simpleimserver.model.ws.WSData;
import com.shixun.simpleimserver.model.ws.WSMsg;
import com.shixun.simpleimserver.netty.UserChannelMap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotifyService {

    @Autowired
    private FriendService friendService;

    /**
     * 异步通知好友状态变更
     * 使用自定义线程池 'statusNotifyExecutor'
     * @param userId 当前发生变化的用户ID
     * @param status "1":上线, "0":下线
     */
    @Async("statusNotifyExecutor")
    public void notifyFriendStatusAsync(Long userId, String status) {
        try {
            // 1. 查出所有好友的 ID
            List<Long> friendIds = friendService.getFriendIdList(userId);

            if (friendIds == null || friendIds.isEmpty()) return;

            // 2. 构建通知消息
            WSMsg notifyMsg = new WSMsg();
            notifyMsg.setType(MsgType.USER_STATUS); // Type = 6
            notifyMsg.setSenderId(userId);          // 谁的状态变了
            WSData data = new WSData();
            data.setContent(status);                // 变成了什么状态
            notifyMsg.setData(data);

            String jsonStr = JSON.toJSONString(notifyMsg);

            // 3. 遍历好友，如果在线就推送
            for (Long friendId : friendIds) {
                Channel friendChannel = UserChannelMap.get(friendId);
                if (friendChannel != null && friendChannel.isActive()) {
                    friendChannel.writeAndFlush(new TextWebSocketFrame(jsonStr));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
