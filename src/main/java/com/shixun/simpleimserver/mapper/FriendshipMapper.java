package com.shixun.simpleimserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shixun.simpleimserver.entity.Friendship;
import org.apache.ibatis.annotations.Mapper;

/**
 * 好友关系表 Mapper 接口
 * 继承 BaseMapper 后，自动拥有了 insert, selectOne, selectList, updateById 等方法
 */
@Mapper
public interface FriendshipMapper extends BaseMapper<Friendship> {

    // 如果将来需要写复杂的自定义 SQL，可以在这里定义方法
    // 并在 resources/mapper/FriendshipMapper.xml 中编写 XML
    // 目前阶段 MVP 不需要写任何代码
}