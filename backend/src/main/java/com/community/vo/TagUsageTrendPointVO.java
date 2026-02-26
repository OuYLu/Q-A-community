package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "标签使用趋势点")
public class TagUsageTrendPointVO {
    @Schema(description = "统计日期")
    private String statDate;

    @Schema(description = "引用次数")
    private Long refCount;
}
