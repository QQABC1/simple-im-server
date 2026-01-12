package com.shixun.simpleimserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shixun.simpleimserver.common.utils.SecurityUtils;
import com.shixun.simpleimserver.entity.Group;
import com.shixun.simpleimserver.entity.GroupMember;
import com.shixun.simpleimserver.mapper.GroupMapper;
import com.shixun.simpleimserver.mapper.GroupMemberMapper;
import com.shixun.simpleimserver.model.dto.GroupCreateDTO;
import com.shixun.simpleimserver.model.vo.GroupVO;
import com.shixun.simpleimserver.service.GroupService;
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
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements GroupService {

    @Autowired
    private GroupMemberMapper groupMemberMapper;

    // 角色常量
    private static final int ROLE_MEMBER = 1;
    private static final int ROLE_OWNER = 3;

    /**
     * 5.1 创建群组
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 开启事务
    public void createGroup(GroupCreateDTO dto) {
        Long userId = SecurityUtils.getUserId();

        // 1. 插入 Group 表
        Group group = new Group();
        group.setGroupName(dto.getGroupName());
        group.setNotice(dto.getNotice());
        group.setOwnerId(userId);
        group.setCreatedAt(LocalDateTime.now());

        // save 方法执行后，MyBatis-Plus 会自动将生成的 ID 回填到 group 对象中
        this.save(group);

        // 2. 插入 GroupMember 表 (把自己设为群主)
        GroupMember member = new GroupMember();
        member.setGroupId(group.getId());
        member.setUserId(userId);
        member.setRole(ROLE_OWNER); // 3: 群主
        member.setJoinTime(LocalDateTime.now());

        groupMemberMapper.insert(member);
    }

    /**
     * 5.2 获取我的群组列表
     */
    @Override
    public List<GroupVO> getMyGroups() {
        Long userId = SecurityUtils.getUserId();

        // 1. 查关系：group_members 表
        // SQL: SELECT * FROM group_members WHERE user_id = ?
        List<GroupMember> memberships = groupMemberMapper.selectList(
                new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getUserId, userId)
        );

        if (memberships.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 提取所有群组 ID
        List<Long> groupIds = memberships.stream()
                .map(GroupMember::getGroupId)
                .collect(Collectors.toList());

        // 3. 查详情：groups 表
        // SQL: SELECT * FROM groups WHERE id IN (...)
        List<Group> groups = this.listByIds(groupIds);

        // 转为 Map 方便匹配 (Key: GroupId, Value: Group)
        Map<Long, Group> groupMap = groups.stream()
                .collect(Collectors.toMap(Group::getId, g -> g));

        // 4. 组装数据 (合并 角色信息 和 群基本信息)
        return memberships.stream().map(m -> {
            Group g = groupMap.get(m.getGroupId());
            if (g == null) return null; // 容错处理

            GroupVO vo = new GroupVO();
            BeanUtils.copyProperties(g, vo); // 复制 id, groupName, notice, ownerId
            vo.setRole(m.getRole());         // 设置我在该群的角色
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 加入群组 (MVP简化版)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinGroup(Long groupId) {
        Long userId = SecurityUtils.getUserId();

        // 1. 校验群是否存在
        Group group = this.getById(groupId);
        if (group == null) {
            throw new RuntimeException("群组不存在");
        }

        // 2. 校验是否已经在群里 (防止重复加入)
        Long count = groupMemberMapper.selectCount(new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, userId));

        if (count > 0) {
            throw new RuntimeException("你已经在这个群里了");
        }

        // 3. 插入成员记录
        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setRole(ROLE_MEMBER); // 1: 普通成员
        member.setJoinTime(LocalDateTime.now());

        groupMemberMapper.insert(member);
    }
}