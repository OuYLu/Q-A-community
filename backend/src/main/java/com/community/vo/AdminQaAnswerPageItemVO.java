package com.community.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminQaAnswerPageItemVO {
    private Long id;
    private Long questionId;
    private String questionTitle;
    private String contentPreview;
    private Integer status;
    private Integer deleteFlag;
    private Long authorId;
    private String authorName;
    private Long questionAuthorId;
    private String questionAuthorName;
    private Integer likeCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
