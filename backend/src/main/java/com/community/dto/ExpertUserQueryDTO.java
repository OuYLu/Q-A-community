package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "专家用户查询")
public class ExpertUserQueryDTO {
    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "机构")
    private String organization;

    @Schema(description = "擅长领域")
    private String expertise;

    @Schema(description = "专家状态（0-禁用，3-认证）")
    private Integer expertStatus;

    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Schema(description = "每页条数")
    private Integer pageSize = 10;
}
