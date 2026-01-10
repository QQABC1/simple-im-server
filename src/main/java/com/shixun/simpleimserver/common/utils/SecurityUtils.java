package com.shixun.simpleimserver.common.utils;

import com.shixun.simpleimserver.config.security.LoginUser; // 引入 LoginUser
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtils{

    /**
     * 获取当前登录用户的 ID
     */
    public static Long getUserId() {
        // 1. 获取 Principal (主体)
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 2. 判断是否是我们自定义的 LoginUser
        if (principal instanceof LoginUser) {
            // 3. 强转并获取 ID
            return ((LoginUser) principal).getId();
        }

        throw new RuntimeException("获取用户ID失败，当前未登录或认证类型不匹配");
    }

    /**
     * 获取当前登录用户的用户名
     */
    public static String getUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal.toString();
    }
}