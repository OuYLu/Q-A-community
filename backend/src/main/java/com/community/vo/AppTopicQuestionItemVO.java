package com.community.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "应用话题问题项")
public class AppTopicQuestionItemVO {
    private Long id;
    private String title;
    private Integer viewCount;
    private Integer answerCount;
    private Integer likeCount;
    private Long authorId;
    private String authorName;
    private String author头像;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}


