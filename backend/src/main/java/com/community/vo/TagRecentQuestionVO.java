package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "使用该标签的最近问题项")
public class TagRecentQuestionVO {
    @Schema(description = "问题编号")
    private Long id;

    @Schema(description = "问题标题")
    private String title;

    @Schema(description = "问题状态")
    private Integer status;

    @Schema(description = "问题创建时间")
    private LocalDateTime createdAt;
}
