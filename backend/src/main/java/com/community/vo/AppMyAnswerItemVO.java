package com.community.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppMyAnswerItemVO {
    private Long answerId;
    private Long questionId;
    private String questionTitle;
    private Integer likeCount;
    private String contentPreview;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
