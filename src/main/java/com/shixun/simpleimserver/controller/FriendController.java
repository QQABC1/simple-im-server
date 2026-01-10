package com.shixun.simpleimserver.controller;

import com.shixun.simpleimserver.common.result.Result;
import com.shixun.simpleimserver.model.dto.FriendAddDTO;
import com.shixun.simpleimserver.model.dto.FriendApproveDTO;
import com.shixun.simpleimserver.model.vo.ContactVO;
import com.shixun.simpleimserver.model.vo.UserVO;
import com.shixun.simpleimserver.service.FriendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friend")
@Api(tags = "好友关系接口") // Knife4j 分组名称
public class FriendController {

    @Autowired
    private FriendService friendService;

    /**
     * 搜索用户
     * GET /api/friend/search?username=xxx
     */
    @GetMapping("/search")
    @ApiOperation("搜索用户 (精确查找)")
    public Result<UserVO> search(@RequestParam String username) {
        UserVO userVO = friendService.searchUser(username);
        return Result.success(userVO);
    }

    /**
     * 发起好友申请
     * POST /api/friend/request
     */
    @PostMapping("/request")
    @ApiOperation("发起添加好友申请")
    public Result<String> sendRequest(@RequestBody FriendAddDTO dto) {
        friendService.sendRequest(dto);
        return Result.success("好友申请已发送");
    }
    /**
     * 获取好友列表
     * POST /api/friend/list
     */
    @GetMapping("/list")
    @ApiOperation("获取好友列表")
    public Result<List<ContactVO>> getFriendList() {
        return Result.success(friendService.getFriendList());
    }
    /**
     * 获取待处理的申请
     * GET /api/friend/pending
     */
    @GetMapping("/pending")
    @ApiOperation("获取待处理的申请")
    public Result<List<ContactVO>> getPending() {
        return Result.success(friendService.getPendingRequests());
    }
    /**
     * 同意/拒绝好友申请
     * POST /api/friend/approve
     */
    @PostMapping("/approve")
    @ApiOperation("同意/拒绝好友申请")
    public Result<String> approve(@RequestBody FriendApproveDTO dto) {
        friendService.approveRequest(dto);
        return Result.success("操作成功");
    }
}