package com.shixun.simpleimserver.common.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应结果封装类
 * 前端收到的 JSON 格式:
 * {
 *   "code": 200,
 *   "msg": "成功",
 *   "data": { ... }
 * }
 * @param <T> 数据载荷的类型
 */
@Data
@NoArgsConstructor // 生成无参构造
@ApiModel(description = "统一响应结果")
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "状态码", example = "200")
    /** 状态码 (200:成功, 500:失败, 401:未登录) */
    private Integer code;

    @ApiModelProperty(value = "提示信息", example = "操作成功")
    /** 提示信息 (如: "登录成功", "密码错误") */
    private String msg;

    @ApiModelProperty(value = "响应数据")
    /** 返回的数据对象 (泛型) */
    private T data;

    // 私有构造，强制使用静态方法创建
    private Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // ============================
    // 成功响应的方法
    // ============================

    /**
     * 成功 - 带数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 成功 - 带数据和自定义消息
     */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(200, msg, data);
    }

    /**
     * 成功 - 不带数据
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    // ============================
    // 失败响应的方法
    // ============================

    /**
     * 失败 - 默认错误码 500
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }

    /**
     * 失败 - 自定义错误码和信息
     */
    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }
}