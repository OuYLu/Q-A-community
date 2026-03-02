package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "专家科普正文内容块")
public class AppExpertContentBlockDTO {
    @Schema(description = "块类型：text/image", example = "text")
    private String type;

    @Schema(description = "文本内容（type=text 时使用）")
    private String text;

    @Schema(description = "图片地址（type=image 时使用）")
    private String url;
}