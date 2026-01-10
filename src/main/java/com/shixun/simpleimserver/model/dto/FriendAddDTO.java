package com.shixun.simpleimserver.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("添加好友申请参数")
public class FriendAddDTO {
    @ApiModelProperty(value = "目标用户ID", required = true)
    private Long targetUserId;

    @ApiModelProperty(value = "验证信息/备注", required = false)
    private String remark;
}