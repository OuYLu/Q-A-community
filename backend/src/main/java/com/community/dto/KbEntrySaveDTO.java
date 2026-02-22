package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "KB entry save")
public class KbEntrySaveDTO {
    @NotNull(message = "categoryId is required")
    private Long categoryId;

    @NotBlank(message = "title is required")
    private String title;

    private String summary;

    @NotBlank(message = "content is required")
    private String content;

    private String source;
    private List<Long> tagIds;
}
