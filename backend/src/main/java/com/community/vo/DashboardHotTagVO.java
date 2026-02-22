package com.community.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DashboardHotTagVO {
    private Long id;
    private String name;
    private Integer source;
    private Integer status;
    private Integer useCount;
    private LocalDateTime createdAt;
}
