package com.community.dto.admin;

import jakarta.validation.constraints.NotBlank;

public class RoleCreateRequest {
    @NotBlank(message = "code 不能为空")
    private String code;
    @NotBlank(message = "name 不能为空")
    private String name;
    private String description;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
