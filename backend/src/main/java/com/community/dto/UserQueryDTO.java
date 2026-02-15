package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "User query")
public class UserQueryDTO {
    @Schema(description = "Username")
    private String username;

    @Schema(description = "Nickname")
    private String nickname;

    @Schema(description = "Status: 1-enabled, 0-disabled")
    private Integer status;

    @Schema(description = "Role code filter (staff/customer/expert)")
    private String roleCode;

    @Schema(description = "Page number")
    private Integer pageNum = 1;

    @Schema(description = "Page size")
    private Integer pageSize = 10;
}
