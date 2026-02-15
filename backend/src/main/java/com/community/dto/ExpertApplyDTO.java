package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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

    @NotNull(message = "proofUrls is required")
    @Valid
    @Schema(description = "Proof materials grouped by type")
    private ProofMaterialsDTO proofUrls;

    @Data
    @Schema(description = "Proof materials groups")
    public static class ProofMaterialsDTO {
        @NotNull(message = "LICENSE is required")
        @Valid
        @Schema(description = "LICENSE materials")
        @com.fasterxml.jackson.annotation.JsonProperty("LICENSE")
        private List<ProofFileDTO> license;

        @NotNull(message = "EMPLOYMENT is required")
        @Valid
        @Schema(description = "EMPLOYMENT materials")
        @com.fasterxml.jackson.annotation.JsonProperty("EMPLOYMENT")
        private List<ProofFileDTO> employment;

        @Valid
        @Schema(description = "TITLE materials")
        @com.fasterxml.jackson.annotation.JsonProperty("TITLE")
        private List<ProofFileDTO> title;

        @Valid
        @Schema(description = "EDUCATION materials")
        @com.fasterxml.jackson.annotation.JsonProperty("EDUCATION")
        private List<ProofFileDTO> education;

        @Valid
        @Schema(description = "OTHER materials")
        @com.fasterxml.jackson.annotation.JsonProperty("OTHER")
        private List<ProofFileDTO> other;
    }

    @Data
    @Schema(description = "Single proof file item")
    public static class ProofFileDTO {
        @NotBlank(message = "url is required")
        @Schema(description = "File URL", example = "https://xx/1.jpg")
        private String url;

        @NotBlank(message = "name is required")
        @Schema(description = "Original file name", example = "执业证.jpg")
        private String name;

        @NotNull(message = "size is required")
        @PositiveOrZero(message = "size must be >= 0")
        @Schema(description = "File size", example = "123456")
        private Long size;

        @NotBlank(message = "mime is required")
        @Schema(description = "MIME type", example = "image/jpeg")
        private String mime;
    }
}
