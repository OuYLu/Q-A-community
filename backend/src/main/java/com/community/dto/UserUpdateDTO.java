package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "User update request")
public class UserUpdateDTO {
    @Schema(description = "Nickname")
    private String nickname;

    @Schema(description = "Phone")
    private String phone;

    @Schema(description = "Email")
    private String email;

    @Schema(description = "Avatar")
    private String avatar;
}
