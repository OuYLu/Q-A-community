package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("qa_favorite")
public class QaFavorite {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long questionId;

    private Long userId;

    private LocalDateTime createdAt;
}