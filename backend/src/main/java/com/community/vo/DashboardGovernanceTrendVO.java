package com.community.vo;

import lombok.Data;

@Data
public class DashboardGovernanceTrendVO {
    private String date;
    private Long reportCount;
    private Long auditCount;
}
