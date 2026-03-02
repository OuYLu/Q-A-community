package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "客户端用户主页")
public class AppUserHomeVO {
    private Long userId;
    private String nickname;
    private String avatar;
    private String slogan;
    private Integer expertStatus;
    private Integer questionCount;
    private Integer answerCount;
    private Integer expertPostCount;
    private Integer followerCount;
    private Integer followingCount;
    private Boolean followed;
    private Boolean self;
}