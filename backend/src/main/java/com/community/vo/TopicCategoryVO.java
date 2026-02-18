package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Topic category item")
public class TopicCategoryVO {
    private Long id;
    private String name;
    private Long parentId;
}
