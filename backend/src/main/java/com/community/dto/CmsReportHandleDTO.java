package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "内容举报处理")
public class CmsReportHandleDTO {
    @NotNull(message = "处理动作不能为空")
    @Min(value = 1, message = "处理动作必须在1到4之间")
    @Max(value = 4, message = "处理动作必须在1到4之间")
    private Integer handleAction;

    private String handleResult;
}
