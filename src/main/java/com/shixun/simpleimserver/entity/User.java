package com.shixun.simpleimserver.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("users") // 对应数据库表名
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO) // 主键自增
    private Long id;

    /** 登录账号 (唯一) */
    private String username;

    /** 密码 (BCrypt加密后的字符串) */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 头像URL */
    private String avatar;

    /** 个性签名 */
    private String signature;

    /** 性别 0:保密 1:男 2:女 */
    private Integer gender;

    /** 注册时间 (自动填充) */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 (自动填充) */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}