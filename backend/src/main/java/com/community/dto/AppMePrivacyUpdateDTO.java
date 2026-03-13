package com.community.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppMePrivacyUpdateDTO {
    @NotNull
    private Integer profileVisible;

    @NotNull
    private Integer statsVisible;

    @NotNull
    private Integer personalizedRecommend;
}
