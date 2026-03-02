package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    // 用户状态：1-正常，0-禁用
    public static final int STATUS_ENABLED = 1;
    public static final int STATUS_DISABLED = 0;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String username;

    private String phone;

    private String password;

    private Integer passwordSet;

    /**
     * 账号状态：1-正常，0-禁用
     */
    private Integer status;

    private String nickname;

    private String avatar;

    private String email;

    private String slogan;

    private Integer followingCount;

    private Integer followerCount;

    private Integer likeReceivedCount;

    /**
     * 专家状态：1-未认证，2-审核中，3-已认证，4-驳回，5-停用/取消认证
     */
    private Integer expertStatus;

    /**
     * 专家认证通过时间（expertStatus=3 时写入）
     */
    private LocalDateTime expertVerifiedAt;

    /**
     * 发布封禁截止时间：当前时间 < banUntil 时，禁止发提问/回答/评论
     */
    private LocalDateTime banUntil;

    /**
     * 发布封禁原因（通常来自举报处理说明）
     */
    private String banReason;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
