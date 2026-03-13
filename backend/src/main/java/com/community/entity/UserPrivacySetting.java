package com.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_privacy_setting")
public class UserPrivacySetting {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer profileVisible;

    private Integer statsVisible;

    private Integer personalizedRecommend;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
