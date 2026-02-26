package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "问答分类保存")
public class QaCategorySaveDTO {
    @NotBlank(message = "名称不能为空")
    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "父分类编号")
    private Long parentId;

    @Schema(description = "分类图标链接")
    private String icon;

    @Schema(description = "分类描述")
    private String description;

    @Min(value = 0, message = "状态必须为0或1")
    @Max(value = 1, message = "状态必须为0或1")
    @Schema(description = "状态，1启用，0禁用")
    private Integer status;

    @Min(value = 0, message = "排序值必须大于等于0")
    @Schema(description = "排序顺序")
    private Integer sort;
}
