package com.shixun.simpleimserver.config.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * 自定义认证用户对象
 * 继承自 Spring Security 的 User，额外增加了 id 字段
 */
@Getter
public class LoginUser extends User {
    private final Long id;

    // 2. 新增这个字段，用来存储完整的用户信息（头像、昵称等）
    private final com.shixun.simpleimserver.entity.User userEntity;

    /**
     * 构造函数
     */
    public LoginUser(com.shixun.simpleimserver.entity.User user, Collection<? extends GrantedAuthority> authorities) {
        // 调用父类构造器
        super(user.getUsername(), user.getPassword(), authorities);

        // 设置 ID
        this.id = user.getId();

        // 3. 【关键】将传入的 user 对象保存到 userEntity 字段中
        this.userEntity = user;
    }
}