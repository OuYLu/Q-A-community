package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用于懒加载的分类树节点")
public class CategoryTreeVO {
    @Schema(description = "分类编号")
    private Long id;

    @Schema(description = "父级编号")
    private Long parentId;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "显示标签")
    private String label;

    @Schema(description = "分类状态")
    private Integer status;

    @Schema(description = "排序顺序")
    private Integer sort;

    @Schema(description = "当前节点是否有子节点")
    private Boolean hasChildren;

    @Schema(description = "当前节点是否为叶子节点")
    private Boolean leaf;
}
