package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "专家申请审核请求")
public class ExpertReviewDTO {
    @NotNull(message = "申请编号不能为空")
    @Schema(description = "申请编号")
    private Long applyId;

    @NotNull(message = "状态不能为空")
    @Schema(description = "审核结果：2-通过，3-驳回", example = "2")
    private Integer status;

    @Schema(description = "驳回原因（状态为3时必填）")
    private String rejectReason;
}
