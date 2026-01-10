package com.shixun.simpleimserver.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("好友联系人信息")
public class ContactVO {
    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("账号")
    private String username;

    @ApiModelProperty("昵称")
    private String nickname;

    @ApiModelProperty("头像")
    private String avatar;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("在线状态")
    private Boolean online;
}