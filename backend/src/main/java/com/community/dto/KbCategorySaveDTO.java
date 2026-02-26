package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "知识库分类保存请求")
public class KbCategorySaveDTO {
    private Long parentId;

    @NotBlank(message = "名称不能为空")
    private String name;

    private String description;
    private String icon;

    @Min(value = 0, message = "排序值必须大于等于0")
    private Integer sort;

    @Min(value = 0, message = "状态必须为0或1")
    @Max(value = 1, message = "状态必须为0或1")
    private Integer status;
}
