package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "话题内最近问题")
public class TopicRecentQuestionVO {
    private Long id;
    private String title;
    private Integer status;
    private LocalDateTime createdAt;
    private Long userId;
    private String authorName;
}
