package com.shixun.simpleimserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shixun.simpleimserver.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}