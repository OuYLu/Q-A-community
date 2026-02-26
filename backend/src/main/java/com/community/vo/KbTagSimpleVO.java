package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "知识库标签简要信息")
public class KbTagSimpleVO {
    private Long id;
    private String name;
}
