package com.shixun.simpleimserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shixun.simpleimserver.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表 DAO 层接口
 * 继承 BaseMapper<User> 后，自动拥有了对 users 表的基础 CRUD 能力
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    // 目前保持为空即可。
    // 如果后续你需要写复杂的连表查询 SQL，可以在这里定义方法，
    // 然后在 resources/mapper/UserMapper.xml 里写对应的 SQL。

}