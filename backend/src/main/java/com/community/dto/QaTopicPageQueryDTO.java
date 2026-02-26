package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Schema(description = "问答话题分页查询")
public class QaTopicPageQueryDTO {
    private String title;
    private Integer status;
    private Long createdBy;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateStart;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateEnd;

    @Schema(description = "关注数/问题数/今日新增/创建时间")
    private String sortBy;

    @Schema(description = "升序/降序")
    private String sortOrder;

    private Integer page = 1;
    private Integer pageSize = 10;
}
