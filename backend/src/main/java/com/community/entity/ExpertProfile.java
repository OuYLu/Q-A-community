package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("expert_profile")
public class ExpertProfile {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String realName;

    private String organization;

    private String title;

    private String expertise;

    private LocalDateTime verifiedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}