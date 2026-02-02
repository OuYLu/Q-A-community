package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("role_permission")
public class RolePermission {
    private Long roleId;

    private Long permissionId;

    private LocalDateTime createdAt;
}