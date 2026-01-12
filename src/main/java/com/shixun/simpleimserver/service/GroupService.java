package com.shixun.simpleimserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shixun.simpleimserver.entity.Group;
import com.shixun.simpleimserver.model.dto.GroupCreateDTO;
import com.shixun.simpleimserver.model.vo.GroupVO;

import java.util.List;

public interface GroupService extends IService<Group> {
    // 创建群
    void createGroup(GroupCreateDTO dto);

    // 获取我的群组列表
    List<GroupVO> getMyGroups();

    // 加入群 (MVP版)
    void joinGroup(Long groupId);
}