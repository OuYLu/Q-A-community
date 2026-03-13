package com.community.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AppMeDataExportVO {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime exportedAt;

    private AppMeOverviewVO overview;

    private AppMePrivacyVO privacy;

    private List<AppMyQuestionItemVO> recentQuestions;

    private List<AppMyAnswerItemVO> recentAnswers;

    private List<AppMyFavoriteItemVO> recentFavorites;

    private List<AppMyHistoryItemVO> recentHistory;

    private List<AppFollowUserItemVO> following;

    private List<AppFollowUserItemVO> followers;

    private List<AppFollowTopicItemVO> followedTopics;
}
