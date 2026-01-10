package com.shixun.simpleimserver.model.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    // 使用 JSR-303 校验注解 (建议引入 hibernate-validator)
    // @NotBlank(message = "账号不能为空")
    private String username;

    // @Length(min = 6, message = "密码至少6位")
    private String password;

    private String nickname; // 注册时可选填昵称
}