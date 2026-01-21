package com.community.dto.admin;

import java.util.List;

public class PermissionGroupDTO {
    private String moduleKey;
    private String moduleName;
    private List<PermissionItemDTO> children;

    public String getModuleKey() {
        return moduleKey;
    }

    public void setModuleKey(String moduleKey) {
        this.moduleKey = moduleKey;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public List<PermissionItemDTO> getChildren() {
        return children;
    }

    public void setChildren(List<PermissionItemDTO> children) {
        this.children = children;
    }
}
