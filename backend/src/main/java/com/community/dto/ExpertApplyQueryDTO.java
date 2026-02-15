package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Expert apply query")
public class ExpertApplyQueryDTO {
    @Schema(description = "Status")
    private Integer status;

    @Schema(description = "Real name")
    private String realName;

    @Schema(description = "Organization")
    private String organization;

    @Schema(description = "Expertise")
    private String expertise;

    @Schema(description = "Page number")
    private Integer pageNum = 1;

    @Schema(description = "Page size")
    private Integer pageSize = 10;
}
