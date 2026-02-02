package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("qa_question")
public class QaQuestion {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long categoryId;

    private Long topicId;

    private String title;

    private String content;

    private Integer status;

    private String rejectReason;

    private Integer viewCount;

    private Integer answerCount;

    private Integer likeCount;

    private Integer favoriteCount;

    private Long acceptedAnswerId;

    private LocalDateTime acceptedAt;

    private LocalDateTime lastActiveAt;

    private Integer deleteFlag;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}