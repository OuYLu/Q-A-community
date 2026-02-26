package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "话题问题分页项")
public class TopicQuestionPageItemVO {
    private Long id;
    private String title;
    private Integer status;
    private LocalDateTime createdAt;
    private Long userId;
}
