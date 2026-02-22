package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "KB category save")
public class KbCategorySaveDTO {
    private Long parentId;

    @NotBlank(message = "name is required")
    private String name;

    private String description;
    private String icon;

    @Min(value = 0, message = "sort must be >= 0")
    private Integer sort;

    @Min(value = 0, message = "status must be 0 or 1")
    @Max(value = 1, message = "status must be 0 or 1")
    private Integer status;
}
