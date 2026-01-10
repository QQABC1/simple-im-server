package com.shixun.simpleimserver.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("处理好友申请参数")
public class FriendApproveDTO {
    @ApiModelProperty(value = "申请记录ID (friendships表的主键)", required = true)
    private Long requestId;

    @ApiModelProperty(value = "是否同意: true同意, false拒绝", required = true)
    private Boolean agree;
}