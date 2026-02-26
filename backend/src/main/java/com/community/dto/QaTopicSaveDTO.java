package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "问答话题保存")
public class QaTopicSaveDTO {
    @NotBlank(message = "标题不能为空")
    private String title;

    private String subtitle;
    private String coverImg;
    private String intro;

    @Min(value = 1, message = "状态必须在1到4之间")
    @Max(value = 4, message = "状态必须在1到4之间")
    private Integer status;

    private List<Long> categoryIds;
}
