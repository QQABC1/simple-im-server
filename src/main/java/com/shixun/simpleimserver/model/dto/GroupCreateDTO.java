package com.shixun.simpleimserver.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("创建群组参数")
public class GroupCreateDTO {
    @ApiModelProperty(value = "群名称", required = true)
    private String groupName;

    @ApiModelProperty(value = "群公告", required = false)
    private String notice;
}