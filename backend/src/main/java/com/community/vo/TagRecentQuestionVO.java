package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Recent question using tag")
public class TagRecentQuestionVO {
    @Schema(description = "Question id")
    private Long id;

    @Schema(description = "Question title")
    private String title;

    @Schema(description = "Question status")
    private Integer status;

    @Schema(description = "Question created time")
    private LocalDateTime createdAt;
}
