package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "应用热门搜索项")
public class AppSearchHotVO {
    private String queryText;
    private Integer hitCount;
}


