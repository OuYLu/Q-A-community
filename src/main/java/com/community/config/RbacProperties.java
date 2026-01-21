package com.community.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rbac")
public class RbacProperties {
    /**
        * 启动时自动注册权限（已存在）
        */
    private boolean permissionAutoRegister = false;

    /**
        * 开发环境自动将所有权限授予 admin 角色
        */
    private boolean autoGrantAdmin = false;

    public boolean isPermissionAutoRegister() {
        return permissionAutoRegister;
    }

    public void setPermissionAutoRegister(boolean permissionAutoRegister) {
        this.permissionAutoRegister = permissionAutoRegister;
    }

    public boolean isAutoGrantAdmin() {
        return autoGrantAdmin;
    }

    public void setAutoGrantAdmin(boolean autoGrantAdmin) {
        this.autoGrantAdmin = autoGrantAdmin;
    }
}
