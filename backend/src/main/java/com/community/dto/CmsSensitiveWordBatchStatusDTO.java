package com.community.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CmsSensitiveWordBatchStatusDTO {
    @NotEmpty(message = "编号列表不能为空")
    private List<Long> ids;
}

