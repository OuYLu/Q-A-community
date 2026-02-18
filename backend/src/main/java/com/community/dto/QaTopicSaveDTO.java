package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "QA topic save")
public class QaTopicSaveDTO {
    @NotBlank(message = "title is required")
    private String title;

    private String subtitle;
    private String coverImg;
    private String intro;

    @Min(value = 1, message = "status must be between 1 and 4")
    @Max(value = 4, message = "status must be between 1 and 4")
    private Integer status;

    private List<Long> categoryIds;
}
