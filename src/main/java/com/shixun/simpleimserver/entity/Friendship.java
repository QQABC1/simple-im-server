package com.shixun.simpleimserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 好友关系实体类
 * 对应数据库表: friendships
 */
@Data // Lombok 注解，自动生成 Getter, Setter, ToString 等
@TableName("friendships") // MyBatis-Plus 注解，指定表名
public class Friendship implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     * type = IdType.AUTO 代表使用数据库的自增策略
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 当前用户ID (主动方)
     */
    private Long userId;

    /**
     * 好友用户ID (被动方)
     */
    private Long friendId;

    /**
     * 好友备注
     */
    private String remark;

    /**
     * 状态
     * 0: 申请中
     * 1: 已添加 (正式好友)
     * 2: 已拒绝
     * 3: 拉黑
     */
    private Integer status;

    /**
     * 创建时间
     * 对应数据库的 datetime 类型
     */
    private LocalDateTime createdAt;
}