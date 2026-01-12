package com.shixun.simpleimserver;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.shixun.simpleimserver.model.dto.GroupCreateDTO;
import com.shixun.simpleimserver.model.dto.GroupJoinDTO;
import com.shixun.simpleimserver.model.dto.LoginDTO;
import com.shixun.simpleimserver.model.dto.RegisterDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // 按顺序执行
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // 允许 @BeforeAll 使用非静态方法
public class GroupModuleTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 全局变量：存储两个用户的 Token 和创建的群组 ID
    private static String tokenOwner;  // 群主 Token
    private static String tokenMember; // 成员 Token
    private static Long groupId;       // 群 ID

    /**
     * 初始化：清空数据库，确保测试环境纯净
     */
    @BeforeAll
    void setup() {
        System.out.println("====== 初始化数据库环境 ======");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("TRUNCATE TABLE users");
        jdbcTemplate.execute("TRUNCATE TABLE `groups`");
        jdbcTemplate.execute("TRUNCATE TABLE group_members");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    /**
     * 步骤 1: 准备两个用户 (UserA 和 UserB) 并登录
     */
    @Test
    @Order(1)
    void step1_prepareUsers() throws Exception {
        // --- 1. 注册并登录 UserA (未来的群主) ---
        registerUser("owner", "123", "我是群主");
        tokenOwner = loginUser("owner", "123");
        System.out.println("群主 Token: " + tokenOwner);

        // --- 2. 注册并登录 UserB (未来的成员) ---
        registerUser("member", "123", "我是成员");
        tokenMember = loginUser("member", "123");
        System.out.println("成员 Token: " + tokenMember);
    }

    /**
     * 步骤 2: UserA 创建群组
     */
    @Test
    @Order(2)
    void step2_createGroup() throws Exception {
        GroupCreateDTO dto = new GroupCreateDTO();
        dto.setGroupName("Java学习交流群");
        dto.setNotice("禁止发广告");

        mockMvc.perform(post("/api/group/create")
                        .header("Authorization", "Bearer " + tokenOwner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("创建成功"));
    }

    /**
     * 步骤 3: UserA 查看群组列表 (验证是否创建成功，并获取 GroupID)
     */
    @Test
    @Order(3)
    void step3_getOwnerGroupList() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/group/list")
                        .header("Authorization", "Bearer " + tokenOwner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].groupName").value("Java学习交流群"))
                .andExpect(jsonPath("$.data[0].role").value(3)) // 验证角色是否为 3 (群主)
                .andDo(print())
                .andReturn();

        // 提取 GroupId 供后续测试使用
        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JSONObject json = JSON.parseObject(content);
        groupId = json.getJSONArray("data").getJSONObject(0).getLong("id");

        System.out.println(">>> 获取到新创建的 GroupID: " + groupId);
    }

    /**
     * 步骤 4: UserB 加入群组
     */
    @Test
    @Order(4)
    void step4_joinGroup() throws Exception {
        GroupJoinDTO dto = new GroupJoinDTO();
        dto.setGroupId(groupId); // 使用步骤3获取的 ID

        mockMvc.perform(post("/api/group/join")
                        .header("Authorization", "Bearer " + tokenMember) // 使用 Member 的 Token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("加入成功"));
    }

    /**
     * 步骤 5: UserB 查看群组列表 (验证是否加入成功)
     */
    @Test
    @Order(5)
    void step5_getMemberGroupList() throws Exception {
        mockMvc.perform(get("/api/group/list")
                        .header("Authorization", "Bearer " + tokenMember))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(groupId))
                .andExpect(jsonPath("$.data[0].groupName").value("Java学习交流群"))
                .andExpect(jsonPath("$.data[0].role").value(1)) // 验证角色是否为 1 (普通成员)
                .andDo(print());
    }

    /**
     * 步骤 6: 异常测试 - 重复加入 (验证业务逻辑)
     */
    @Test
    @Order(6)
    void step6_duplicateJoin() throws Exception {
        GroupJoinDTO dto = new GroupJoinDTO();
        dto.setGroupId(groupId);

        // 再次尝试加入，应该报错
        mockMvc.perform(post("/api/group/join")
                        .header("Authorization", "Bearer " + tokenMember)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500)) // 期望返回错误码
                .andExpect(jsonPath("$.msg").value("你已经在这个群里了")); // 期望返回错误信息
    }

    // ================= 辅助方法 =================

    private void registerUser(String username, String password, String nickname) throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername(username);
        dto.setPassword(password);
        dto.setNickname(nickname);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(dto)))
                .andExpect(status().isOk());
    }

    private String loginUser(String username, String password) throws Exception {
        LoginDTO dto = new LoginDTO();
        dto.setUsername(username);
        dto.setPassword(password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return JSON.parseObject(content).getJSONObject("data").getString("token");
    }
}