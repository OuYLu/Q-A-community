package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Tag usage trend point")
public class TagUsageTrendPointVO {
    @Schema(description = "Stat date, yyyy-MM-dd")
    private String statDate;

    @Schema(description = "Reference count")
    private Long refCount;
}
