package com.community.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AppMeCancelRequestDTO {
    @Size(max = 500)
    private String reason;
}
