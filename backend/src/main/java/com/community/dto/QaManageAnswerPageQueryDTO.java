package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Schema(description = "后台问答管理-回答分页查询")
public class QaManageAnswerPageQueryDTO {
    private String keyword;
    private Integer status;
    private Integer deleteFlag = 0;
    private Long questionId;
    private Long userId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;

    private String sortBy;
    private String sortOrder;
    private Integer page = 1;
    private Integer pageSize = 10;
}
