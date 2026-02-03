package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Expert certification apply request")
public class ExpertApplyDTO {
    @NotBlank(message = "realName is required")
    @Schema(description = "Real name", example = "Li Hua")
    private String realName;

    @Schema(description = "Organization", example = "City Hospital")
    private String organization;

    @Schema(description = "Title", example = "Chief Physician")
    private String title;

    @NotBlank(message = "expertise is required")
    @Schema(description = "Expertise", example = "Cardiology")
    private String expertise;

    @NotEmpty(message = "proofUrls is required")
    @Schema(description = "Proof URLs", example = "[\"https://example.com/cert1.png\"]")
    private List<String> proofUrls;
}
