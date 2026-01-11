package com.shixun.simpleimserver.controller;

import com.shixun.simpleimserver.common.result.Result;
import com.shixun.simpleimserver.model.dto.UserPasswordDTO;
import com.shixun.simpleimserver.model.dto.UserUpdateDTO;
import com.shixun.simpleimserver.model.vo.UserVO;
import com.shixun.simpleimserver.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Api(tags = "个人中心接口")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    @ApiOperation("获取当前登录用户信息")
    public Result<UserVO> getUserInfo() {
        return Result.success(userService.getCurrentUserInfo());
    }

    /**
     * 修改资料
     */
    @PostMapping("/update")
    @ApiOperation("修改个人资料 (昵称/头像/签名/性别)")
    public Result<String> updateInfo(@RequestBody UserUpdateDTO dto) {
        userService.updateUserInfo(dto);
        return Result.success("修改成功");
    }

    /**
     * 修改密码
     */
    @PostMapping("/password")
    @ApiOperation("修改登录密码")
    public Result<String> updatePassword(@RequestBody UserPasswordDTO dto) {
        try {
            userService.updateUserPassword(dto);
            return Result.success("密码修改成功，请重新登录");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}