package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "KB tag simple")
public class KbTagSimpleVO {
    private Long id;
    private String name;
}
