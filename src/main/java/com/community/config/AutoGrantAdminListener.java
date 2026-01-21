package com.community.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.domain.permission.Permission;
import com.community.domain.role.Role;
import com.community.domain.role.RolePermission;
import com.community.mapper.PermissionMapper;
import com.community.mapper.RoleMapper;
import com.community.mapper.RolePermissionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Profile({"local", "dev"})
public class AutoGrantAdminListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(AutoGrantAdminListener.class);

    @Autowired
    private RbacProperties rbacProperties;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!rbacProperties.isAutoGrantAdmin()) {
            return;
        }

        Role adminRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getCode, "admin"));
        if (adminRole == null) {
            log.warn("[RBAC] auto-grant-admin enabled, but admin role not found");
            return;
        }

        List<Permission> permissions = permissionMapper.selectList(null);
        if (permissions.isEmpty()) {
            log.info("[RBAC] auto-grant-admin: no permissions to grant");
            return;
        }
        Set<Long> allPermIds = permissions.stream().map(Permission::getId).collect(Collectors.toSet());

        List<RolePermission> existing = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, adminRole.getId()));
        Set<Long> owned = existing.stream().map(RolePermission::getPermissionId).collect(Collectors.toSet());

        Set<Long> missing = new HashSet<>(allPermIds);
        missing.removeAll(owned);
        if (missing.isEmpty()) {
            log.info("[RBAC] auto-grant-admin: already up-to-date, inserted 0");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        int count = 0;
        for (Long pid : missing) {
            RolePermission rp = new RolePermission();
            rp.setRoleId(adminRole.getId());
            rp.setPermissionId(pid);
            rp.setCreatedAt(now);
            rolePermissionMapper.insert(rp);
            count++;
        }
        log.info("[RBAC] auto-grant-admin: inserted {} missing permissions for admin", count);
    }
}
