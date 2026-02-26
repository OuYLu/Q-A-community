package com.community.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppMyHistoryItemVO {
    private Integer bizType;
    private Long bizId;
    private String title;
    private String sub职称;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime viewedAt;
}

