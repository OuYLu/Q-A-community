package com.community.config;

import com.community.common.annotation.RequirePermission;
import com.community.domain.permission.Permission;
import com.community.mapper.PermissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "rbac", name = "permission-auto-register", havingValue = "true")
public class PermissionAutoRegisterRunner implements ApplicationRunner {

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @Autowired
    private PermissionMapper permissionMapper;

    @Override
    public void run(ApplicationArguments args) {
        // load existing permission codes
        Set<String> existingCodes = permissionMapper.selectList(null).stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());

        List<Permission> toInsert = new ArrayList<>();
        List<Permission> toUpdate = new ArrayList<>();

        handlerMapping.getHandlerMethods().forEach((RequestMappingInfo info, HandlerMethod method) -> {
            RequirePermission rp = method.getMethodAnnotation(RequirePermission.class);
            if (rp == null) {
                return;
            }
            String code = rp.value();
            String name = rp.name();
            if (code == null || code.isEmpty()) {
                return;
            }
            if (existingCodes.contains(code)) {
                // 已存在：只有当注解提供了 name 时更新 name
                if (name != null && !name.isEmpty()) {
                    Permission p = permissionMapper.selectOne(
                            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Permission>().eq("code", code));
                    if (p != null && (p.getName() == null || !p.getName().equals(name))) {
                        p.setName(name);
                        toUpdate.add(p);
                    }
                }
                return;
            }
            Permission permission = new Permission();
            permission.setCode(code);
            permission.setName(name == null ? "" : name);
            permission.setType("API");
            permission.setPathOrApi(resolvePath(info));
            permission.setMethod(resolveMethods(info));
            permission.setCreatedAt(LocalDateTime.now());
            toInsert.add(permission);
            existingCodes.add(code);
        });

        if (!toInsert.isEmpty()) {
            toInsert.forEach(permissionMapper::insert);
        }

        if (!toUpdate.isEmpty()) {
            toUpdate.forEach(permissionMapper::updateById);
        }
    }

    private String resolvePath(RequestMappingInfo info) {
        if (info.getPathPatternsCondition() != null && info.getPathPatternsCondition().getPatterns() != null) {
            return info.getPathPatternsCondition().getPatterns().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
        }
        if (info.getPatternsCondition() != null && info.getPatternsCondition().getPatterns() != null) {
            return info.getPatternsCondition().getPatterns().stream()
                    .collect(Collectors.joining(","));
        }
        return "";
    }

    private String resolveMethods(RequestMappingInfo info) {
        if (info.getMethodsCondition() != null && info.getMethodsCondition().getMethods() != null) {
            return info.getMethodsCondition().getMethods().stream()
                    .map(Enum::name)
                    .collect(Collectors.joining(","));
        }
        return "";
    }
}
