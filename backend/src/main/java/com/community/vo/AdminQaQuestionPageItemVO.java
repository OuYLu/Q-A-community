package com.community.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminQaQuestionPageItemVO {
    private Long id;
    private String title;
    private String summary;
    private Integer status;
    private Integer deleteFlag;
    private Long categoryId;
    private String categoryName;
    private Long topicId;
    private String topicTitle;
    private Long authorId;
    private String authorName;
    private Integer answerCount;
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
