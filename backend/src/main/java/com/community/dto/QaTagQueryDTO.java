package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "QA tag query")
public class QaTagQueryDTO {
    @Schema(description = "Tag name")
    private String name;

    @Schema(description = "Source: 1 system, 2 user")
    private Integer source;

    @Schema(description = "Status")
    private Integer status;

    @Min(value = 0, message = "useCountMin must be >= 0")
    @Schema(description = "Use count min (inclusive)")
    private Integer useCountMin;

    @Min(value = 0, message = "useCountMax must be >= 0")
    @Schema(description = "Use count max (inclusive)")
    private Integer useCountMax;

    @Schema(description = "Page number")
    private Integer pageNum = 1;

    @Schema(description = "Page size")
    private Integer pageSize = 10;
}
