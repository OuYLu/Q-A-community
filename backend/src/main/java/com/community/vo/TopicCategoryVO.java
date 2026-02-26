package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "话题分类项")
public class TopicCategoryVO {
    private Long id;
    private String name;
    private Long parentId;
}
