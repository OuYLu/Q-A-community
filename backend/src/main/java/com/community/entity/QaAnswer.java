package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("qa_answer")
public class QaAnswer {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long questionId;

    private Long userId;

    private String content;

    private Integer status;

    private String rejectReason;

    private Integer likeCount;

    private Integer isAnonymous;

    private Integer deleteFlag;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}