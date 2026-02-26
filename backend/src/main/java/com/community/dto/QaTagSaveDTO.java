package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "问答标签保存")
public class QaTagSaveDTO {
    @NotBlank(message = "名称不能为空")
    @Schema(description = "标签名称")
    private String name;

    @NotNull(message = "来源不能为空")
    @Min(value = 1, message = "来源必须为1或2")
    @Max(value = 2, message = "来源必须为1或2")
    @Schema(description = "来源：1系统，2用户")
    private Integer source;

    @Min(value = 0, message = "状态必须为0或1")
    @Max(value = 1, message = "状态必须为0或1")
    @Schema(description = "状态：1启用，0禁用")
    private Integer status;
}
