package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "专家科普文章发布/更新参数")
public class AppExpertPostCreateDTO {
    @NotBlank(message = "标题不能为空")
    @Schema(description = "标题", example = "长期熬夜后如何科学补觉")
    private String title;

    @Schema(description = "摘要（可选）")
    private String summary;

    @Schema(description = "正文纯文本（兼容字段，可为空）")
    private String content;

    @Schema(description = "科普分类ID（必填）", example = "1")
    private Long categoryId;

    @Schema(description = "封面图URL（可选）")
    private String coverImage;

    @Schema(description = "正文图片URL列表（兼容字段，可选）")
    private List<String> imageUrls;

    @Valid
    @Schema(description = "正文内容块（支持 text/image 交替）")
    private List<AppExpertContentBlockDTO> contentBlocks;

    @Schema(description = "标签名列表（可选）")
    private List<String> tagNames;
}