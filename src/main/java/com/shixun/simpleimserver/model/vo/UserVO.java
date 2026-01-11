package com.shixun.simpleimserver.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("用户详细信息")
public class UserVO {
    @ApiModelProperty("用户ID")
    private Long id;

    @ApiModelProperty("账号")
    private String username;

    @ApiModelProperty("昵称")
    private String nickname;

    @ApiModelProperty("头像URL")
    private String avatar;

    @ApiModelProperty("个性签名")
    private String signature;

    @ApiModelProperty("性别 0:保密 1:男 2:女")
    private Integer gender;
}