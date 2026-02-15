package com.community.vo;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExpertApplyDetailVO {
    private String realName;
    private String organization;
    private String title;
    private String expertise;
    private JsonNode proofUrls;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
