package com.shixun.simpleimserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shixun.simpleimserver.entity.User;
import com.shixun.simpleimserver.model.dto.LoginDTO;
import com.shixun.simpleimserver.model.dto.RegisterDTO;

/**
 * 用户业务逻辑接口
 * 继承 IService<User> 后，自动获得 MyBatis-Plus 提供的基础 CRUD 功能
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param dto 注册参数(账号、密码、昵称)
     */
    void register(RegisterDTO dto);


}