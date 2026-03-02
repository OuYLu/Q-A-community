package com.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AppQuestionReportCreateDTO {
    @NotBlank(message = "举报原因不能为空")
    @Size(max = 64, message = "举报原因编码长度不能超过64")
    private String reasonCode;

    @Size(max = 500, message = "举报说明长度不能超过500")
    private String reasonDetail;
}

