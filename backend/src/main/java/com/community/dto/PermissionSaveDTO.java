package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "权限增删查改请求")
public class PermissionSaveDTO {
    @NotBlank(message = "code is required")
    @Schema(description = "Permission code", example = "rbac:user:manage")
    private String code;

    @NotBlank(message = "name is required")
    @Schema(description = "Permission name", example = "User manage")
    private String name;

    @NotBlank(message = "type is required")
    @Schema(description = "Type (menu/button/api)", example = "api")
    private String type;

    @Schema(description = "Path or API", example = "/api/admin/users")
    private String pathOrApi;

    @Schema(description = "HTTP method", example = "GET")
    private String method;
}
