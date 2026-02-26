package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "专家状态更新")
public class ExpertStatusUpdateDTO {
    @NotNull(message = "专家状态不能为空")
    @Schema(description = "专家状态：3-认证，0-禁用", example = "0")
    private Integer expertStatus;
}
