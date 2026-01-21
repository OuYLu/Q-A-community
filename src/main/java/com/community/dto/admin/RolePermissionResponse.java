package com.community.dto.admin;

import java.util.List;

public class RolePermissionResponse {
    private List<String> codes;
    private List<PermissionItemDTO> items;
    private List<PermissionGroupDTO> groups;

    public RolePermissionResponse(List<String> codes, List<PermissionItemDTO> items, List<PermissionGroupDTO> groups) {
        this.codes = codes;
        this.items = items;
        this.groups = groups;
    }

    public List<String> getCodes() {
        return codes;
    }

    public void setCodes(List<String> codes) {
        this.codes = codes;
    }

    public List<PermissionItemDTO> getItems() {
        return items;
    }

    public void setItems(List<PermissionItemDTO> items) {
        this.items = items;
    }

    public List<PermissionGroupDTO> getGroups() {
        return groups;
    }

    public void setGroups(List<PermissionGroupDTO> groups) {
        this.groups = groups;
    }
}
