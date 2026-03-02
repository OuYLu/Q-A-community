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
    @Schema(description = "处理动作：1-下架，2-警告，3-封禁，4-不处理")
    private Integer handleAction;

    @Schema(description = "处理结果说明")
    private String handleResult;

    @Schema(description = "封禁天数，仅 handleAction=3 时生效，默认3天")
    @Min(value = 1, message = "封禁天数至少1天")
    @Max(value = 3650, message = "封禁天数不能超过3650天")
    private Integer banDays;
}