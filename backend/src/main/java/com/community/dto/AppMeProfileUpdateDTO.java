package com.community.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AppMeProfileUpdateDTO {
    @Size(max = 64)
    private String nickname;

    @Size(max = 255)
    private String avatar;

    @Size(max = 255)
    private String slogan;

    @Size(max = 100)
    private String email;
}
