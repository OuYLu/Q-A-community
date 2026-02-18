package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Recent question in topic")
public class TopicRecentQuestionVO {
    private Long id;
    private String title;
    private Integer status;
    private LocalDateTime createdAt;
    private Long userId;
    private String authorName;
}
