package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "应用搜索结果")
public class AppSearchResultVO {
    private String query;
    private List<AppSearchQuestionVO> questions;
    private List<AppSearchTopicVO> topics;
    private List<AppSearchTagVO> tags;
}


