package com.shixun.simpleimserver.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("修改密码参数")
public class UserPasswordDTO {
    @ApiModelProperty(value = "旧密码", required = true)
    private String oldPassword;

    @ApiModelProperty(value = "新密码", required = true)
    private String newPassword;
}