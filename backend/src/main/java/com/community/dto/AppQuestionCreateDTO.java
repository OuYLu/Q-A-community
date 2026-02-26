package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "客户端创建问题")
public class AppQuestionCreateDTO {
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200")
    @Schema(description = "问题标题")
    private String title;

    @Size(max = 20000, message = "问题内容过长")
    @Schema(description = "问题内容")
    private String content;

    @Schema(description = "分类编号")
    private Long categoryId;

    @Schema(description = "话题编号")
    private Long topicId;

    @Schema(description = "标签编号列表")
    private List<Long> tagIds;

    @Schema(description = "tag names")
    private List<String> tagNames;

    @Schema(description = "问题图片URL列表")
    private List<String> imageUrls;
}
