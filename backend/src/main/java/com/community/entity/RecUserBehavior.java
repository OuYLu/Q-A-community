package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@TableName("rec_user_behavior")
public class RecUserBehavior {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer actionType;

    private Long targetId;

    private Long categoryId;

    private BigDecimal weight;

    private LocalDateTime createdAt;
}