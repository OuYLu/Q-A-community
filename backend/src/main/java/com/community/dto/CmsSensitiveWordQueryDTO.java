package com.community.dto;

import lombok.Data;

@Data
public class CmsSensitiveWordQueryDTO {
    private String keyword;
    private Integer level;
    private Integer enabled;
    private String category;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}

