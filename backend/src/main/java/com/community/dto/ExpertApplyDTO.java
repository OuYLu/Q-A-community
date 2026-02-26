package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "专家认证申请请求")
public class ExpertApplyDTO {
    @NotBlank(message = "真实姓名不能为空")
    @Schema(description = "真实姓名", example = "Li Hua")
    private String realName;

    @Schema(description = "机构", example = "City Hospital")
    private String organization;

    @Schema(description = "职称", example = "Chief Physician")
    private String title;

    @NotBlank(message = "擅长领域不能为空")
    @Schema(description = "擅长领域", example = "Cardiology")
    private String expertise;

    @NotNull(message = "证明材料不能为空")
    @Valid
    @Schema(description = "按类型分组的证明材料")
    private ProofMaterialsDTO proofUrls;

    @Data
    @Schema(description = "证明材料分组")
    public static class ProofMaterialsDTO {
        @NotNull(message = "执业资质材料不能为空")
        @Valid
        @Schema(description = "执业资质材料")
        @com.fasterxml.jackson.annotation.JsonProperty("LICENSE")
        private List<ProofFileDTO> license;

        @NotNull(message = "在职证明材料不能为空")
        @Valid
        @Schema(description = "在职证明材料")
        @com.fasterxml.jackson.annotation.JsonProperty("EMPLOYMENT")
        private List<ProofFileDTO> employment;

        @Valid
        @Schema(description = "职称材料")
        @com.fasterxml.jackson.annotation.JsonProperty("TITLE")
        private List<ProofFileDTO> title;

        @Valid
        @Schema(description = "教育材料")
        @com.fasterxml.jackson.annotation.JsonProperty("EDUCATION")
        private List<ProofFileDTO> education;

        @Valid
        @Schema(description = "其他材料")
        @com.fasterxml.jackson.annotation.JsonProperty("OTHER")
        private List<ProofFileDTO> other;
    }

    @Data
    @Schema(description = "单个证明文件项")
    public static class ProofFileDTO {
        @NotBlank(message = "URL不能为空")
        @Schema(description = "文件链接", example = "https://xx/1.jpg")
        private String url;

        @NotBlank(message = "名称不能为空")
        @Schema(description = "原始文件名", example = "执业证.jpg")
        private String name;

        @NotNull(message = "大小不能为空")
        @PositiveOrZero(message = "size must be >= 0")
        @Schema(description = "文件大小", example = "123456")
        private Long size;

        @NotBlank(message = "MIME类型不能为空")
        @Schema(description = "媒体类型", example = "image/jpeg")
        private String mime;
    }
}
