package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "问答标签批量状态更新")
public class QaTagBatchStatusDTO {
    @NotNull(message = "编号列表不能为空")
    @NotEmpty(message = "编号列表不能为空")
    @Schema(description = "标签编号列表")
    private List<Long> ids;
}
