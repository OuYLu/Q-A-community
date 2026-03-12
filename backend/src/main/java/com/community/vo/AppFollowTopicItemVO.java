package com.community.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppFollowTopicItemVO {
    private Long topicId;
    private String title;
    private String subtitle;
    private String coverImg;
    private Integer followCount;
    private Integer questionCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime followedAt;
}
