package com.community.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Answer comment item")
public class AppAnswerCommentVO {
    @Schema(description = "Comment id")
    private Long id;

    @Schema(description = "Answer id")
    private Long answerId;

    @Schema(description = "Parent comment id")
    private Long parentId;

    @Schema(description = "Author id")
    private Long authorId;

    @Schema(description = "Author name")
    private String authorName;

    @Schema(description = "Author avatar")
    private String authorAvatar;

    @Schema(description = "Comment content")
    private String content;

    @Schema(description = "Created time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
