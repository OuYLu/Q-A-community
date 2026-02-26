package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "应用专家卡片")
public class AppExpertCardVO {
    private Long userId;
    private String nickname;
    private String avatar;
    private String realName;
    private String organization;
    private String title;
    private String expertise;
}

