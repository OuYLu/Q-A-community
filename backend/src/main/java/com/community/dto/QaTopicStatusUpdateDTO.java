package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "问答话题状态更新")
public class QaTopicStatusUpdateDTO {
    @NotNull(message = "状态不能为空")
    @Min(value = 1, message = "状态必须在1到4之间")
    @Max(value = 4, message = "状态必须在1到4之间")
    private Integer status;
}
