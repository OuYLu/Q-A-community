package com.community.vo;

import lombok.Data;

@Data
public class DashboardContentTrendVO {
    private String date;
    private Long questionCount;
    private Long answerCount;
    private Long commentCount;
}
