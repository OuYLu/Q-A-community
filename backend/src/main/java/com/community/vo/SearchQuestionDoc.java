package com.community.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SearchQuestionDoc {
    private Long id;
    private String title;
    private String content;
    private Long categoryId;
    private Long topicId;
    private Integer answerCount;
    private Integer status;
    private LocalDateTime createdAt;
}