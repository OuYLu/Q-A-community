package com.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_oauth")
public class UserOauth {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String provider;

    private String openid;

    private String unionid;

    private String appId;

    private String sessionKey;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
