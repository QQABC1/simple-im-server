package com.shixun.simpleimserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shixun.simpleimserver.common.utils.JwtUtil; // 1. 记得引入 JwtUtil
import com.shixun.simpleimserver.common.utils.SecurityUtils;
import com.shixun.simpleimserver.entity.User;
import com.shixun.simpleimserver.mapper.UserMapper;
import com.shixun.simpleimserver.model.dto.LoginDTO;
import com.shixun.simpleimserver.model.dto.RegisterDTO;
import com.shixun.simpleimserver.model.dto.UserPasswordDTO;
import com.shixun.simpleimserver.model.dto.UserUpdateDTO;
import com.shixun.simpleimserver.model.vo.UserVO;
import com.shixun.simpleimserver.service.UserService;
import org.springframework.beans.BeanUtils;
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

    /**
     * 1. 获取当前用户信息
     */
    @Override
    public UserVO getCurrentUserInfo() {
        // 直接利用 SecurityUtils 获取当前 ID (无需查库)
        Long userId = SecurityUtils.getUserId();

        // 查库获取最新详情
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 转 VO
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    /**
     * 2. 修改个人资料
     */
    @Override
    public void updateUserInfo(UserUpdateDTO dto) {
        Long userId = SecurityUtils.getUserId();

        // 创建一个只包含 ID 和需要修改字段的 User 对象
        // MyBatis-Plus 的 updateById 方法会自动忽略 null 字段，只更新非 null 字段
        User updateEntity = new User();
        updateEntity.setId(userId);

        // 只有当前端传了值时，才设置进去
        if (dto.getNickname() != null) updateEntity.setNickname(dto.getNickname());
        if (dto.getAvatar() != null) updateEntity.setAvatar(dto.getAvatar());
        if (dto.getSignature() != null) updateEntity.setSignature(dto.getSignature());
        if (dto.getGender() != null) updateEntity.setGender(dto.getGender());

        this.updateById(updateEntity);
    }

    /**
     * 3. 修改密码
     */
    @Override
    public void updateUserPassword(UserPasswordDTO dto) {
        Long userId = SecurityUtils.getUserId();

        // 必须查出数据库里的密文密码用于比对
        User user = this.getById(userId);

        // 校验旧密码: matches(明文, 数据库密文)
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        // 加密新密码并更新
        String newEncodedPass = passwordEncoder.encode(dto.getNewPassword());

        User updateEntity = new User();
        updateEntity.setId(userId);
        updateEntity.setPassword(newEncodedPass);

        this.updateById(updateEntity);
    }


}