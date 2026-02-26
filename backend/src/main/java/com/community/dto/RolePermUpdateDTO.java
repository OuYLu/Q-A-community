package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "角色权限更新请求")
public class RolePermUpdateDTO {
    @NotNull(message = "角色编号不能为空")
    @Schema(description = "角色编号")
    private Long roleId;

    @Schema(description = "权限编号列表")
    private List<Long> permissionIds;
}
