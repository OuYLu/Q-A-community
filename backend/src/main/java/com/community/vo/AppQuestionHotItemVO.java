package com.community.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "应用热门问题项")
public class AppQuestionHotItemVO {
    private Long id;
    private String title;
    private Integer viewCount;
    private Integer answerCount;
    private Integer likeCount;
    private Long authorId;
    private String authorName;
    private String authorAvatar;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
