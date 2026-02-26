package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "内容审核批量处理")
public class CmsAuditBatchReviewDTO {
    @NotNull(message = "编号列表不能为空")
    @NotEmpty(message = "编号列表不能为空")
    private List<Long> ids;

    @NotBlank(message = "操作不能为空")
    @Schema(description = "通过/驳回")
    private String action;

    private String rejectReason;
}
