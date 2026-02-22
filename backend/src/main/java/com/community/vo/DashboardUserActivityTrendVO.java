package com.community.vo;

import lombok.Data;

@Data
public class DashboardUserActivityTrendVO {
    private String date;
    private Long newUserCount;
    private Long activeUserCount;
    private Long searchCount;
}
