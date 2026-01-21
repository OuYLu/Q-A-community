package com.community.dto.admin;

import jakarta.validation.constraints.NotNull;

public class UpdateUserStatusRequest {
    @NotNull(message = "status 不能为空")
    private Integer status;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
