package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "专家状态更新请求")
public class ExpertStatusUpdateDTO {
    @NotNull(message = "专家状态不能为空")
    @Schema(description = "专家状态：3-已认证，5-禁用专家权限", example = "5")
    private Integer expertStatus;
}
