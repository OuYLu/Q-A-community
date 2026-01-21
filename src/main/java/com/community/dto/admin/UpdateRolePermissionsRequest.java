package com.community.dto.admin;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class UpdateRolePermissionsRequest {
    @NotEmpty(message = "permissionCodes 不能为空")
    private List<String> permissionCodes;

    public List<String> getPermissionCodes() {
        return permissionCodes;
    }

    public void setPermissionCodes(List<String> permissionCodes) {
        this.permissionCodes = permissionCodes;
    }
}
