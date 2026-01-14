/*
 Navicat Premium Dump SQL
 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 50700 (5.7.xx) -- 修改版本号以匹配你的环境
 Source Host           : localhost:3306
 Source Schema         : simple_im

 Target Server Type    : MySQL
 Target Server Version : 50700 (5.7.xx)
 File Encoding         : 65001

 Date: 14/01/2026 00:42:52
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for friendships
-- ----------------------------
DROP TABLE IF EXISTS `friendships`;
CREATE TABLE `friendships`  (
                                `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                `user_id` bigint(20) NOT NULL COMMENT '用户ID (主动方)',
                                `friend_id` bigint(20) NOT NULL COMMENT '好友ID (被动方)',
                                `remark` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '好友备注',
                                `status` tinyint(4) NULL DEFAULT 0 COMMENT '状态 0:申请中 1:已添加 2:已拒绝 3:拉黑',
                                `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`) USING BTREE,
                                UNIQUE INDEX `uk_user_friend`(`user_id`, `friend_id`) USING BTREE,
                                INDEX `idx_user_status`(`user_id`, `status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='好友关系表';

-- ----------------------------
-- Table structure for group_members
-- ----------------------------
DROP TABLE IF EXISTS `group_members`;
CREATE TABLE `group_members`  (
                                  `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                  `group_id` bigint(20) NOT NULL COMMENT '群ID',
                                  `user_id` bigint(20) NOT NULL COMMENT '成员ID',
                                  `role` tinyint(4) NULL DEFAULT 1 COMMENT '角色 1:普通成员 2:管理员 3:群主',
                                  `join_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                                  PRIMARY KEY (`id`) USING BTREE,
                                  UNIQUE INDEX `uk_group_user`(`group_id`, `user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='群成员关联表';

-- ----------------------------
-- Table structure for groups
-- ----------------------------
DROP TABLE IF EXISTS `groups`;
CREATE TABLE `groups`  (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '群组ID',
                           `owner_id` bigint(20) NOT NULL COMMENT '群主用户ID',
                           `group_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '群名称',
                           `notice` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '群公告',
                           `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                           PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='群组基本信息表';

-- ----------------------------
-- Table structure for messages
-- ----------------------------
DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages`  (
                             `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
                             `from_id` bigint(20) NOT NULL COMMENT '发送者ID',
                             `to_id` bigint(20) NOT NULL COMMENT '接收者ID (好友ID 或 群ID)',
                             `session_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '会话类型 1:单聊 2:群聊',
                             `msg_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '消息类型 1:纯文本 2:富文本(带字体) 3:文件/图片 4:窗口抖动',
                             `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '消息内容/文件地址/JSON',
                             `send_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
                             `status` tinyint(4) NULL DEFAULT 0 COMMENT '状态 0:未读 1:已读 2:撤回',
                             PRIMARY KEY (`id`) USING BTREE,
                             INDEX `idx_session`(`from_id`, `to_id`, `session_type`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='聊天消息表';

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
                          `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                          `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录账号(唯一)',
                          `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码(存储Hash值/MD5)',
                          `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '昵称',
                          `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像URL',
                          `signature` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '个性签名',
                          `gender` tinyint(4) NULL DEFAULT 0 COMMENT '性别 0:保密 1:男 2:女',
                          `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                          `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          PRIMARY KEY (`id`) USING BTREE,
                          UNIQUE INDEX `uk_username`(`username`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户信息表';

SET FOREIGN_KEY_CHECKS = 1;