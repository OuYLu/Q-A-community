package com.community.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DashboardHotTopicVO {
    private Long id;
    private String title;
    private Integer status;
    private Integer followCount;
    private Integer questionCount;
    private Integer todayNewCount;
    private LocalDateTime createdAt;
}
