package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "QA topic categories update")
public class QaTopicCategoryUpdateDTO {
    private List<Long> categoryIds;
}
