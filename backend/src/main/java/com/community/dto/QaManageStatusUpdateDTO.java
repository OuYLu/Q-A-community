package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "后台问答管理-状态更新")
public class QaManageStatusUpdateDTO {
    @NotNull
    @Schema(description = "状态：1-发布，4-下架", example = "4")
    private Integer status;
}
