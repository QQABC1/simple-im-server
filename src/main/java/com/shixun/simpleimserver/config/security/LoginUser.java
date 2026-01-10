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

    private final Long id; // 我们新增的字段

    /**
     * 构造函数
     * @param user 我们数据库里的 User 实体
     * @param authorities 权限列表
     */
    public LoginUser(com.shixun.simpleimserver.entity.User user, Collection<? extends GrantedAuthority> authorities) {
        // 调用父类构造器设置 username, password, authorities
        super(user.getUsername(), user.getPassword(), authorities);
        // 设置我们的 id
        this.id = user.getId();
    }
}