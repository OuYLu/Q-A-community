package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "权限增删查改请求")
public class PermissionSaveDTO {
    @NotBlank(message = "编码不能为空")
    @Schema(description = "权限编码", example = "rbac:user:manage")
    private String code;

    @NotBlank(message = "名称不能为空")
    @Schema(description = "权限名称", example = "User manage")
    private String name;

    @NotBlank(message = "类型不能为空")
    @Schema(description = "类型（菜单/按钮/接口）", example = "api")
    private String type;

    @Schema(description = "路径或接口", example = "/api/admin/users")
    private String pathOrApi;

    @Schema(description = "请求方法", example = "GET")
    private String method;
}
