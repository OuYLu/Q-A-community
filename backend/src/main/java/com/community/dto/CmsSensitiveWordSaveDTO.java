package com.community.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CmsSensitiveWordSaveDTO {
    @NotBlank(message = "敏感词不能为空")
    @Size(max = 100, message = "敏感词长度不能超过100")
    private String word;

    @Min(value = 1, message = "敏感级别不正确")
    @Max(value = 2, message = "敏感级别不正确")
    private Integer level;

    @Size(max = 50, message = "分类长度不能超过50")
    private String category;

    @Size(max = 200, message = "命中提示长度不能超过200")
    private String hitActionDesc;

    @Size(max = 255, message = "拦截提示长度不能超过255")
    private String reasonTemplate;

    @Min(value = 0, message = "启用状态不正确")
    @Max(value = 1, message = "启用状态不正确")
    private Integer enabled;
}

