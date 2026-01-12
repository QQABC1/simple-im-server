package com.shixun.simpleimserver.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("加入群组参数")
public class GroupJoinDTO {
    @ApiModelProperty(value = "群组ID", required = true)
    private Long groupId;
}