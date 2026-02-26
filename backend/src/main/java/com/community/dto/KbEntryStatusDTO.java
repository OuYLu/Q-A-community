package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "知识库条目状态更新请求")
public class KbEntryStatusDTO {
    @NotNull(message = "状态不能为空")
    @Min(value = 1, message = "状态必须为1或4")
    @Max(value = 4, message = "状态必须为1或4")
    private Integer status;
}
