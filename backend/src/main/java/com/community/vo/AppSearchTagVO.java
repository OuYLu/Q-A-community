package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "应用搜索标签项")
public class AppSearchTagVO {
    private Long id;
    private String name;
    private Integer useCount;
}


