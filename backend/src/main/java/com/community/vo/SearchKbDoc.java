package com.community.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SearchKbDoc {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private String source;
    private LocalDateTime createdAt;
}