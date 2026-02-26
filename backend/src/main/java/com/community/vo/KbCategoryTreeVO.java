package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "知识库分类树")
public class KbCategoryTreeVO {
    private Long id;
    private Long parentId;
    private String name;
    private String description;
    private String icon;
    private Integer sort;
    private Integer status;
    private List<KbCategoryTreeVO> children = new ArrayList<>();
}
