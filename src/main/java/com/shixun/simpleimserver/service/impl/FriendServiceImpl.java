package com.shixun.simpleimserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shixun.simpleimserver.common.utils.SecurityUtils;
import com.shixun.simpleimserver.entity.Friendship;
import com.shixun.simpleimserver.entity.User;
import com.shixun.simpleimserver.mapper.FriendshipMapper;
import com.shixun.simpleimserver.mapper.UserMapper;
import com.shixun.simpleimserver.model.dto.FriendAddDTO;
import com.shixun.simpleimserver.model.dto.FriendApproveDTO;
import com.shixun.simpleimserver.model.vo.ContactVO;
import com.shixun.simpleimserver.model.vo.UserVO;
import com.shixun.simpleimserver.service.FriendService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FriendServiceImpl implements FriendService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FriendshipMapper friendshipMapper;

    /**
     * 搜索用户逻辑
     */
    @Override
    public UserVO searchUser(String username) {
        // 1. 查询数据库
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 转换为 VO (隐藏密码等敏感字段)
        UserVO vo = new UserVO();
        // 使用 BeanUtils 快速复制同名属性 (id, username, nickname, avatar)
        BeanUtils.copyProperties(user, vo);

        return vo;
    }

    /**
     * 发起好友申请逻辑
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 开启事务
    public void sendRequest(FriendAddDTO dto) {
        // 1. 获取当前用户
        Long currentUserId = SecurityUtils.getUserId();

        Long targetUserId = dto.getTargetUserId();

        // 2. 基础校验
        if (currentUserId.equals(targetUserId)) {
            throw new RuntimeException("不能添加自己为好友");
        }

        // 校验目标用户是否存在
        User targetUser = userMapper.selectById(targetUserId);
        if (targetUser == null) {
            throw new RuntimeException("目标用户不存在");
        }

        // 3. 查重 (检查是否已经是好友，或已发送申请)
        Friendship existing = friendshipMapper.selectOne(new LambdaQueryWrapper<Friendship>()
                .eq(Friendship::getUserId, currentUserId)
                .eq(Friendship::getFriendId, targetUserId));

        if (existing != null) {
            if (existing.getStatus() == 1) {
                throw new RuntimeException("你们已经是好友了");
            }
            if (existing.getStatus() == 0) {
                throw new RuntimeException("已发送过申请，请等待对方同意");
            }
            // 如果是 2(已拒绝)，允许再次申请，继续往下走
        }

        // 4. 插入记录 (A -> B, Status=0 申请中)
        // 如果之前有记录（比如被拒绝过），则更新；否则插入新记录
        if (existing == null) {
            Friendship friendship = new Friendship();
            friendship.setUserId(currentUserId);
            friendship.setFriendId(targetUserId);
            friendship.setRemark(dto.getRemark());
            friendship.setStatus(0); // 0: 申请中
            friendship.setCreatedAt(LocalDateTime.now());
            friendshipMapper.insert(friendship);
        } else {
            // 更新旧记录状态
            existing.setStatus(0);
            existing.setRemark(dto.getRemark());
            existing.setCreatedAt(LocalDateTime.now()); // 更新申请时间
            friendshipMapper.updateById(existing);
        }

        // 5. 【TODO】 WebSocket 实时通知
        // 此处应调用 Netty 发送系统消息给 targetUserId
        // pushService.sendSystemMsg(targetUserId, "有人请求添加你为好友");
    }

    /**
     * 获取好友列表
     */
    @Override
    public List<ContactVO> getFriendList() {
        // 获取当前用户ID (从 SecurityUtils 获取)
        Long currentUserId = SecurityUtils.getUserId();

        // 2. 查库：friendships 表 (user_id = 我 AND status = 1)
        List<Friendship> list = friendshipMapper.selectList(new LambdaQueryWrapper<Friendship>()
                .eq(Friendship::getUserId, currentUserId)
                .eq(Friendship::getStatus, 1)); // 1 代表已添加

        if (list.isEmpty()) {
            return new ArrayList<>();
        }

        // 3. 提取好友ID列表
        List<Long> friendIds = list.stream().map(Friendship::getFriendId).collect(Collectors.toList());

        // 4. 查库：users 表 (批量查好友详情)
        List<User> users = userMapper.selectBatchIds(friendIds);
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));

        // 5. 组装数据
        return list.stream().map(f -> {
            User u = userMap.get(f.getFriendId());
            ContactVO vo = new ContactVO();
            vo.setUserId(u.getId());
            vo.setUsername(u.getUsername());
            vo.setNickname(u.getNickname());
            vo.setAvatar(u.getAvatar());
            vo.setRemark(f.getRemark());

            // TODO: 此处后续对接 Netty 获取真实在线状态
            vo.setOnline(false); // 目前默认为离线

            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取待处理的申请 (别人加我)
     */
    @Override
    public List<ContactVO> getPendingRequests() {
        // 获取当前用户ID (从 SecurityUtils 获取)
        Long currentUserId = SecurityUtils.getUserId();

        // 2. 查库：friendships 表 (friend_id = 我 AND status = 0)
        // 意思是：发起人是别人(user_id)，接收人是我(friend_id)，状态是申请中
        List<Friendship> requests = friendshipMapper.selectList(new LambdaQueryWrapper<Friendship>()
                .eq(Friendship::getFriendId, currentUserId)
                .eq(Friendship::getStatus, 0));

        if (requests.isEmpty()) {
            return new ArrayList<>();
        }

        // 3. 提取发起人ID
        List<Long> senderIds = requests.stream().map(Friendship::getUserId).collect(Collectors.toList());
        List<User> senders = userMapper.selectBatchIds(senderIds);
        Map<Long, User> senderMap = senders.stream().collect(Collectors.toMap(User::getId, u -> u));

        // 4. 组装数据
        return requests.stream().map(req -> {
            User sender = senderMap.get(req.getUserId());
            ContactVO vo = new ContactVO();
            // 这里放的是发起申请那条记录的主键ID，处理申请时要传这个ID
            // 注意：ContactVO 没有 requestId 字段，实际开发建议加一个字段，或者前端传 userId 也可以
            // 这里为了简单，前端通过 userId 来同意也可以，或者复用 userId 字段传回去
            vo.setUserId(req.getId()); // ⚠️注意：这里临时把 申请记录ID 放在 userId 字段传给前端，方便前端调用 approve 接口传 ID

            // 正常展示信息
            vo.setUsername(sender.getUsername());
            vo.setNickname(sender.getNickname());
            vo.setAvatar(sender.getAvatar());
            vo.setRemark(req.getRemark()); // 这里显示对方填写的验证消息
            vo.setOnline(false);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 处理好友申请 (同意/拒绝)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveRequest(FriendApproveDTO dto) {
        // 1. 获取当前用户ID
        Long currentUserId = SecurityUtils.getUserId();

        // 2. 查申请记录
        Friendship request = friendshipMapper.selectById(dto.getRequestId());
        if (request == null) {
            throw new RuntimeException("申请记录不存在");
        }
        if (!request.getFriendId().equals(currentUserId)) {
            throw new RuntimeException("无权处理此申请");
        }

        if (dto.getAgree()) {
            // === 同意 ===
            // 1. 更新对方的记录 (A -> B) 状态改为 1
            request.setStatus(1);
            request.setCreatedAt(LocalDateTime.now());
            friendshipMapper.updateById(request);

            // 2. 插入/更新我的记录 (B -> A) 状态改为 1
            Friendship reverse = friendshipMapper.selectOne(new LambdaQueryWrapper<Friendship>()
                    .eq(Friendship::getUserId, currentUserId)
                    .eq(Friendship::getFriendId, request.getUserId()));

            if (reverse == null) {
                reverse = new Friendship();
                reverse.setUserId(currentUserId);
                reverse.setFriendId(request.getUserId());
                reverse.setStatus(1); // 已添加
                reverse.setCreatedAt(LocalDateTime.now());
                friendshipMapper.insert(reverse);
            } else {
                reverse.setStatus(1);
                friendshipMapper.updateById(reverse);
            }

            // TODO: 调用 Netty 发送 WebSocket 消息通知申请方：“对方已同意”

        } else {
            // === 拒绝 ===
            request.setStatus(2); // 2 代表已拒绝
            friendshipMapper.updateById(request);
        }
    }
}