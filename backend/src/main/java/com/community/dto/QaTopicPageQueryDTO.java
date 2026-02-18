package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Schema(description = "QA topic page query")
public class QaTopicPageQueryDTO {
    private String title;
    private Integer status;
    private Long createdBy;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateStart;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateEnd;

    @Schema(description = "followCount/questionCount/todayNewCount/createdAt")
    private String sortBy;

    @Schema(description = "asc/desc")
    private String sortOrder;

    private Integer page = 1;
    private Integer pageSize = 10;
}
