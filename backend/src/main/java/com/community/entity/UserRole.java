package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_role")
public class UserRole {
    private Long userId;

    private Long roleId;

    private LocalDateTime createdAt;
}