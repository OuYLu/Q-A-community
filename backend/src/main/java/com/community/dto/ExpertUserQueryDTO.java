package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Expert user query")
public class ExpertUserQueryDTO {
    @Schema(description = "Real name")
    private String realName;

    @Schema(description = "Organization")
    private String organization;

    @Schema(description = "Expertise")
    private String expertise;

    @Schema(description = "Expert status (0-disabled, 3-certified)")
    private Integer expertStatus;

    @Schema(description = "Page number")
    private Integer pageNum = 1;

    @Schema(description = "Page size")
    private Integer pageSize = 10;
}
