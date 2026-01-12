package com.shixun.simpleimserver.controller;

import com.shixun.simpleimserver.common.result.Result;
import com.shixun.simpleimserver.common.utils.JwtUtil;
import com.shixun.simpleimserver.config.security.LoginUser;
import com.shixun.simpleimserver.model.dto.LoginDTO;
import com.shixun.simpleimserver.model.dto.RegisterDTO;
import com.shixun.simpleimserver.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Api(tags = "认证接口")  // 控制器分组
public class AuthController {

    @Autowired
    private UserService userService; // 处理注册

    @Autowired
    private AuthenticationManager authenticationManager; // 处理登录验证

    @Autowired
    private JwtUtil jwtUtil; // 生成Token

    @Autowired
    private UserDetailsService userDetailsService; // 加载用户信息

    /**
     * 注册接口
     */
    @PostMapping("/register")
    @ApiOperation("用户注册")  // 接口描述
    public Result<String> register(@RequestBody RegisterDTO dto) {
        try {
            userService.register(dto);
            return Result.success("注册成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 登录接口
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")  // 接口描述
    public Result<Map<String, Object>> login(@RequestBody LoginDTO dto) {
        try {
            // 1. 验证 (内部触发第1次查询)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
            );

            // 2. 【优化点】直接从认证结果中获取用户信息，不要再去查数据库
            // getPrincipal() 返回的就是 UserDetailsServiceImpl 里返回的对象
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // 3. 生成 Token
            String token = jwtUtil.generateToken(userDetails);

            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            // 4. 封装返回结果 (前端不仅需要Token，通常也需要展示用户头像昵称)
            // 这里需要再去查一次 User 实体获取头像昵称，或者 modify UserDetailsServiceImpl 让它携带更多信息
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            map.put("tokenHead", "Bearer "); // 前端请求头拼接用
            map.put("username", userDetails.getUsername());
            map.put("id",loginUser.getId());

            return Result.success(map);

        } catch (BadCredentialsException e) {
            return Result.error("用户名或密码错误");
        }

    }
}