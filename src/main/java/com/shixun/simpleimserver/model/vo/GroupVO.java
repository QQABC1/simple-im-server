package com.shixun.simpleimserver.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("群组列表信息")
public class GroupVO {
    @ApiModelProperty("群组ID")
    private Long id;

    @ApiModelProperty("群名称")
    private String groupName;

    @ApiModelProperty("群公告")
    private String notice;

    @ApiModelProperty("群主ID")
    private Long ownerId;

    @ApiModelProperty("我在群里的角色 (1:成员 2:管理员 3:群主)")
    private Integer role;
}