package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String username;

    private String phone;

    private String password;

    private Integer status;

    private String nickname;

    private String avatar;

    private String email;

    private Integer followingCount;

    private Integer followerCount;

    private Integer likeReceivedCount;

    private Integer expertStatus;

    private LocalDateTime expertVerifiedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}