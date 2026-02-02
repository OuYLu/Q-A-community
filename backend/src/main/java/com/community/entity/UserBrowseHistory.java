package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_browse_history")
public class UserBrowseHistory {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer bizType;

    private Long bizId;

    private LocalDateTime createdAt;
}