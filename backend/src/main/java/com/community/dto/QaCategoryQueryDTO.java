package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "问答分类查询")
public class QaCategoryQueryDTO {
    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "父分类编号")
    private Long parentId;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Schema(description = "每页条数")
    private Integer pageSize = 10;
}
