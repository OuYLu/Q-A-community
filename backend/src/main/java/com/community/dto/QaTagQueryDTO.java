package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "问答标签查询")
public class QaTagQueryDTO {
    @Schema(description = "标签名称")
    private String name;

    @Schema(description = "来源：1系统，2用户")
    private Integer source;

    @Schema(description = "状态")
    private Integer status;

    @Min(value = 0, message = "最小使用次数必须大于等于0")
    @Schema(description = "最小使用次数（含）")
    private Integer useCountMin;

    @Min(value = 0, message = "最大使用次数必须大于等于0")
    @Schema(description = "最大使用次数（含）")
    private Integer useCountMax;

    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Schema(description = "每页条数")
    private Integer pageSize = 10;
}
