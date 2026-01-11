package com.shixun.simpleimserver.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("修改个人资料参数")
public class UserUpdateDTO {
    @ApiModelProperty(value = "新昵称", required = false)
    private String nickname;

    @ApiModelProperty(value = "新头像URL", required = false)
    private String avatar;

    @ApiModelProperty(value = "新个性签名", required = false)
    private String signature;

    @ApiModelProperty(value = "性别 0:保密 1:男 2:女", required = false)
    private Integer gender;
}