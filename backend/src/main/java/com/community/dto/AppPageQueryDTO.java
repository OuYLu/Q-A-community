package com.community.dto;

import lombok.Data;

@Data
public class AppPageQueryDTO {
    private Integer page = 1;
    private Integer pageSize = 10;
}
