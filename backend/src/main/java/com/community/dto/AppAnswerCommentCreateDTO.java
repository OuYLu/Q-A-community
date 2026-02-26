package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Create answer comment DTO")
public class AppAnswerCommentCreateDTO {
    @Positive(message = "parentId must be positive")
    @Schema(description = "Parent comment id for reply")
    private Long parentId;

    @NotBlank(message = "comment content is required")
    @Size(max = 1000, message = "comment content is too long")
    @Schema(description = "Comment content")
    private String content;
}
