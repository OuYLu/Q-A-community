package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "话题问题分页查询")
public class QaTopicQuestionPageQueryDTO {
    private Integer status;
    private String title;
    private String sortBy;
    private String sortOrder;
    private Integer page = 1;
    private Integer pageSize = 10;
}
