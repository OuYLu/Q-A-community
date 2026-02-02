package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@TableName("rec_user_interest")
public class RecUserInterest {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long categoryId;

    private BigDecimal score;

    private LocalDateTime updatedAt;
}