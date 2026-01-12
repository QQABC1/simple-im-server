package com.shixun.simpleimserver;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.shixun.simpleimserver.model.dto.LoginDTO;
import com.shixun.simpleimserver.model.dto.RegisterDTO;
import com.shixun.simpleimserver.model.dto.UserPasswordDTO;
import com.shixun.simpleimserver.model.dto.UserUpdateDTO;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 需清空数据库执行
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // 强制按 @Order 顺序执行
public class UserProfileTest {

    @Autowired
    private MockMvc mockMvc;

    // 全局静态变量，用于在不同测试方法间传递 Token
    private static String token;

    // 定义测试账号信息
    private static final String TEST_USERNAME = "profile_tester";
    private static final String ORIGINAL_PASSWORD = "password123";
    private static final String NEW_PASSWORD = "password888";

    /**
     * 第一步：注册新用户
     */
    @Test
    @Order(1)
    void step1_register() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername(TEST_USERNAME);
        registerDTO.setPassword(ORIGINAL_PASSWORD);
        registerDTO.setNickname("初始昵称");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(registerDTO)))
                .andExpect(status().isOk()) // 期望返回 200
                .andExpect(jsonPath("$.code").value(200))
                .andDo(print()); // 打印请求详情
    }

    /**
     * 第二步：登录并获取 Token
     */
    @Test
    @Order(2)
    void step2_loginAndGetToken() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(TEST_USERNAME);
        loginDTO.setPassword(ORIGINAL_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();

        // 解析 Token
        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JSONObject jsonObject = JSON.parseObject(content);
        token = jsonObject.getJSONObject("data").getString("token");

        System.out.println(">>> 获取到 Token: " + token);
    }

    /**
     * 第三步：获取个人资料 (验证初始数据)
     */
    @Test
    @Order(3)
    void step3_getUserInfo() throws Exception {
        mockMvc.perform(get("/api/user/info")
                        .header("Authorization", "Bearer " + token)) // 带上 Token
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value(TEST_USERNAME))
                .andExpect(jsonPath("$.data.nickname").value("初始昵称"))
                .andDo(print());
    }

    /**
     * 第四步：修改个人资料
     */
    @Test
    @Order(4)
    void step4_updateUserInfo() throws Exception {
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setNickname("修改后的昵称");
        updateDTO.setSignature("我是新的个性签名");
        updateDTO.setGender(1); // 设置为男

        // 1. 执行修改
        mockMvc.perform(post("/api/user/update")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("操作成功"));

        // 2. 再次查询以验证修改是否生效
        mockMvc.perform(get("/api/user/info")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("修改后的昵称"))
                .andExpect(jsonPath("$.data.signature").value("我是新的个性签名"))
                .andExpect(jsonPath("$.data.gender").value(1));
    }

    /**
     * 第五步：修改密码
     */
    @Test
    @Order(5)
    void step5_changePassword() throws Exception {
        UserPasswordDTO passwordDTO = new UserPasswordDTO();
        passwordDTO.setOldPassword(ORIGINAL_PASSWORD); // 旧密码
        passwordDTO.setNewPassword(NEW_PASSWORD);      // 新密码

        mockMvc.perform(post("/api/user/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(passwordDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 第六步：验证密码修改结果
     * (尝试用旧密码登录失败，用新密码登录成功)
     */
    @Test
    @Order(6)
    void step6_verifyNewPassword() throws Exception {
        // 1. 尝试用旧密码登录 -> 期望失败 (500)
        LoginDTO oldLogin = new LoginDTO();
        oldLogin.setUsername(TEST_USERNAME);
        oldLogin.setPassword(ORIGINAL_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(oldLogin)))
                .andExpect(status().isOk()) // HTTP状态码还是200，但业务状态码是错误
                .andExpect(jsonPath("$.code").value(500)) // 我们的 Result.error 默认是 500
                .andExpect(jsonPath("$.msg").value("用户名或密码错误"));

        // 2. 尝试用新密码登录 -> 期望成功
        LoginDTO newLogin = new LoginDTO();
        newLogin.setUsername(TEST_USERNAME);
        newLogin.setPassword(NEW_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(newLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}