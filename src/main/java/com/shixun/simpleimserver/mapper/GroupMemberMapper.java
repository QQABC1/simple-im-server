package com.shixun.simpleimserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shixun.simpleimserver.entity.GroupMember;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GroupMemberMapper extends BaseMapper<GroupMember> {
}