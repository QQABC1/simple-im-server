package com.shixun.simpleimserver.controller;

import com.shixun.simpleimserver.common.result.Result;
import com.shixun.simpleimserver.model.dto.GroupCreateDTO;
import com.shixun.simpleimserver.model.dto.GroupJoinDTO;
import com.shixun.simpleimserver.model.vo.GroupVO;
import com.shixun.simpleimserver.service.GroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group")
@Api(tags = "群组管理接口")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @PostMapping("/create")
    @ApiOperation("创建群组")
    public Result<String> create(@RequestBody GroupCreateDTO dto) {
        groupService.createGroup(dto);
        return Result.success("创建成功");
    }

    @GetMapping("/list")
    @ApiOperation("获取我的群组列表")
    public Result<List<GroupVO>> list() {
        return Result.success(groupService.getMyGroups());
    }

    @PostMapping("/join")
    @ApiOperation("加入群组")
    public Result<String> join(@RequestBody GroupJoinDTO dto) {
        groupService.joinGroup(dto.getGroupId());
        return Result.success("加入成功");
    }
}