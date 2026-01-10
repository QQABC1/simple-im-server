package com.shixun.simpleimserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shixun.simpleimserver.common.utils.JwtUtil; // 1. 记得引入 JwtUtil
import com.shixun.simpleimserver.entity.User;
import com.shixun.simpleimserver.mapper.UserMapper;
import com.shixun.simpleimserver.model.dto.LoginDTO;
import com.shixun.simpleimserver.model.dto.RegisterDTO;
import com.shixun.simpleimserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager; // 2. 记得引入 AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager; // 用于验证账号密码

    @Autowired
    private JwtUtil jwtUtil; // 用于生成 Token


    @Override
    public void register(RegisterDTO dto) {
        // 1. 校验账号是否已存在
        Long count = this.lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .count();
        if (count > 0) {
            throw new RuntimeException("账号已存在");
        }

        // 2. 数据封装
        User user = new User();
        user.setUsername(dto.getUsername());

        // 必须加密存储
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        // 3. 设置默认值
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : "用户" + dto.getUsername());
        // 使用随机头像服务，看起来更真实一点
        user.setAvatar("https://api.dicebear.com/7.x/miniavs/svg?seed=" + dto.getUsername());
        user.setGender(0);

        // 4. 写入数据库
        this.save(user);
    }


}