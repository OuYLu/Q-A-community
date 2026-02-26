package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "专家申请查询")
public class ExpertApplyQueryDTO {
    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "机构")
    private String organization;

    @Schema(description = "擅长领域")
    private String expertise;

    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Schema(description = "每页条数")
    private Integer pageSize = 10;
}
