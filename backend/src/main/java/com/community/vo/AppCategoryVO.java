package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "应用分类项")
public class AppCategoryVO {
    private Long id;
    private String name;
    private String icon;
    private Integer questionCount;
}

