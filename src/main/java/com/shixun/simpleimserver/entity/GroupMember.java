package com.shixun.simpleimserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("group_members")
public class GroupMember {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long groupId;

    private Long userId;

    /** 角色 1:普通成员 2:管理员 3:群主 */
    private Integer role;

    private LocalDateTime joinTime;
}