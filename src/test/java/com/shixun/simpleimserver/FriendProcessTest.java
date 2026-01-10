package com.shixun.simpleimserver;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.shixun.simpleimserver.model.dto.FriendAddDTO;
import com.shixun.simpleimserver.model.dto.FriendApproveDTO;
import com.shixun.simpleimserver.model.dto.LoginDTO;
import com.shixun.simpleimserver.model.dto.RegisterDTO;
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

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * 测试前必须清空数据库中数据
 */


@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // 按顺序执行测试方法
public class FriendProcessTest {

    @Autowired
    private MockMvc mockMvc;

    // 定义两个全局变量存 Token 和 ID
    private static String tokenA; // 张三
    private static String tokenB; // 李四
    private static Long userIdB;  // 李四的ID
    private static Long requestId; // 申请记录ID

    // 1. 注册两个用户
    @Test
    @Order(1)
    void step1_registerUsers() throws Exception {
        // 注册张三
        RegisterDTO userA = new RegisterDTO();
        userA.setUsername("zhangsan_test");
        userA.setPassword("123456");
        userA.setNickname("张三");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(userA)))
                .andExpect(status().isOk());

        // 注册李四
        RegisterDTO userB = new RegisterDTO();
        userB.setUsername("lisi_test");
        userB.setPassword("123456");
        userB.setNickname("李四");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(userB)))
                .andExpect(status().isOk());
    }

    // 2. 登录并拿到 Token
    @Test
    @Order(2)
    void step2_loginAndGetToken() throws Exception {
        // 张三登录
        LoginDTO loginA = new LoginDTO();
        loginA.setUsername("zhangsan_test");
        loginA.setPassword("123456");

        MvcResult resultA = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(loginA)))
                .andExpect(status().isOk())
                .andReturn();

        // 解析响应拿到 Token
        String contentA = resultA.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JSONObject jsonA = JSON.parseObject(contentA);
        tokenA = jsonA.getJSONObject("data").getString("token");
        System.out.println("拿到张三 Token: " + tokenA);

        // 李四登录
        LoginDTO loginB = new LoginDTO();
        loginB.setUsername("lisi_test");
        loginB.setPassword("123456");

        MvcResult resultB = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(loginB)))
                .andExpect(status().isOk())
                .andReturn();

        String contentB = resultB.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JSONObject jsonB = JSON.parseObject(contentB);
        tokenB = jsonB.getJSONObject("data").getString("token");
        System.out.println("拿到李四 Token: " + tokenB);
    }

    // 3. 张三搜索李四，获取李四 ID
    @Test
    @Order(3)
    void step3_searchUser() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/friend/search")
                        .header("Authorization", "Bearer " + tokenA)
                        .param("username", "lisi_test"))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JSONObject json = JSON.parseObject(content);
        userIdB = json.getJSONObject("data").getLong("id");
        System.out.println("搜到李四 ID: " + userIdB);
    }

    // 4. 张三发起申请
    @Test
    @Order(4)
    void step4_sendRequest() throws Exception {
        FriendAddDTO dto = new FriendAddDTO();
        dto.setTargetUserId(userIdB);
        dto.setRemark("我是张三，加个好友");

        mockMvc.perform(post("/api/friend/request")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // 5. 李四查看待处理申请
    @Test
    @Order(5)
    void step5_checkPending() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/friend/pending")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JSONObject json = JSON.parseObject(content);
        // 获取列表第一条的 userId (其实是 requestId)
        requestId = json.getJSONArray("data").getJSONObject(0).getLong("userId");
        System.out.println("拿到申请单 ID: " + requestId);
    }

    // 6. 李四同意申请
    @Test
    @Order(6)
    void step6_approveRequest() throws Exception {
        FriendApproveDTO dto = new FriendApproveDTO();
        dto.setRequestId(requestId);
        dto.setAgree(true);

        mockMvc.perform(post("/api/friend/approve")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(dto)))
                .andExpect(status().isOk());
    }

    // 7. 最终验证：李四的好友列表里应该有张三
    @Test
    @Order(7)
    void step7_checkFriendList() throws Exception {
        mockMvc.perform(get("/api/friend/list")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data[0].username").value("zhangsan_test"));
    }
}