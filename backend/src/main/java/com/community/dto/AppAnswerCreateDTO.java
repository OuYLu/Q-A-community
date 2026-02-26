package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Client create answer DTO")
public class AppAnswerCreateDTO {
    @NotBlank(message = "answer content is required")
    @Size(max = 20000, message = "answer content is too long")
    @Schema(description = "Answer content")
    private String content;

    @Schema(description = "Anonymous flag: 1-anonymous, 0-normal")
    private Integer isAnonymous;

    @Schema(description = "Answer image URL list")
    private List<String> imageUrls;
}