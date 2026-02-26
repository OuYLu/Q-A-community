package com.community.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppMeOverviewVO {
    private Long userId;
    private String username;
    private String phone;
    private String email;
    private Integer expertStatus;
    private Integer passwordSet;
    private String nickname;
    private String avatar;
    private String slogan;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinedAt;

    private Integer questionCount;
    private Integer answerCount;
    private Integer likeReceivedCount;
    private Integer followerCount;
    private Integer followingCount;

    private Integer favoriteCount;
    private Integer historyCount;
}


