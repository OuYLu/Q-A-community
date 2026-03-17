package com.community.service.impl;

import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.common.SecurityUser;
import com.community.entity.Permission;
import com.community.mapper.PermissionMapper;
import com.community.service.MenuService;
import com.community.vo.AdminMenuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {
    private static final String CONTENT_MENU_CODE = "menu:content";
    private static final String QA_MANAGE_MENU_CODE = "menu:content:qa";
    private static final String SENSITIVE_MENU_CODE = "menu:content:sensitive";
    private static final Set<String> CONTENT_RELATED_MENU_CODES = Set.of(
        "menu:content:report",
        "menu:content:audit",
        SENSITIVE_MENU_CODE
    );

    private final PermissionMapper permissionMapper;

    @Override
    public List<AdminMenuVO> listCurrentUserMenus() {
        SecurityUser securityUser = getCurrentSecurityUser();
        if (securityUser == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "请先登录");
        }

        List<Permission> menus = permissionMapper.selectMenusByUserId(securityUser.getId());
        appendQaManageMenu(menus);
        appendSensitiveMenu(menus);
        return buildTree(menus);
    }

    private SecurityUser getCurrentSecurityUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser securityUser)) {
            return null;
        }
        return securityUser;
    }

    private List<AdminMenuVO> buildTree(List<Permission> menus) {
        Map<Long, AdminMenuVO> map = new HashMap<>();
        List<AdminMenuVO> roots = new ArrayList<>();

        for (Permission menu : menus) {
            AdminMenuVO node = toVO(menu);
            map.put(node.getId(), node);
        }

        for (AdminMenuVO node : map.values()) {
            Long parentId = node.getParentId();
            if (parentId == null || !map.containsKey(parentId)) {
                roots.add(node);
            } else {
                map.get(parentId).getChildren().add(node);
            }
        }

        return roots;
    }

    private AdminMenuVO toVO(Permission menu) {
        AdminMenuVO vo = new AdminMenuVO();
        vo.setId(menu.getId());
        vo.setCode(menu.getCode());
        vo.setName(menu.getName());
        vo.setType(menu.getType());
        vo.setParentId(menu.getParentId());
        vo.setPathOrApi(menu.getPathOrApi());
        vo.setComponent(menu.getComponent());
        vo.setIcon(menu.getIcon());
        vo.setSort(menu.getSort());
        vo.setVisible(menu.getVisible());
        return vo;
    }

    private void appendQaManageMenu(List<Permission> menus) {
        if (menus == null || menus.isEmpty()) {
            return;
        }
        boolean qaMenuExists = menus.stream().anyMatch(item -> QA_MANAGE_MENU_CODE.equals(item.getCode()));
        if (qaMenuExists) {
            return;
        }
        Permission contentRoot = menus.stream()
            .filter(item -> CONTENT_MENU_CODE.equals(item.getCode()))
            .findFirst()
            .orElse(null);
        if (contentRoot == null || contentRoot.getId() == null) {
            return;
        }
        boolean hasContentMenu = menus.stream().anyMatch(item -> CONTENT_RELATED_MENU_CODES.contains(item.getCode()));
        if (!hasContentMenu) {
            return;
        }
        Permission qaMenu = new Permission();
        qaMenu.setId(-1001L);
        qaMenu.setCode(QA_MANAGE_MENU_CODE);
        qaMenu.setName("问答管理");
        qaMenu.setType("menu");
        qaMenu.setParentId(contentRoot.getId());
        qaMenu.setPathOrApi("/content/qa");
        qaMenu.setSort(15);
        qaMenu.setVisible(1);
        menus.add(qaMenu);
    }

    private void appendSensitiveMenu(List<Permission> menus) {
        if (menus == null || menus.isEmpty()) {
            return;
        }
        boolean exists = menus.stream().anyMatch(item -> SENSITIVE_MENU_CODE.equals(item.getCode()));
        if (exists) {
            return;
        }
        Permission contentRoot = menus.stream()
            .filter(item -> CONTENT_MENU_CODE.equals(item.getCode()))
            .findFirst()
            .orElse(null);
        if (contentRoot == null || contentRoot.getId() == null) {
            return;
        }
        boolean hasContentMenu = menus.stream().anyMatch(item ->
            "menu:content:report".equals(item.getCode()) || "menu:content:audit".equals(item.getCode()));
        if (!hasContentMenu) {
            return;
        }
        Permission menu = new Permission();
        menu.setId(-1002L);
        menu.setCode(SENSITIVE_MENU_CODE);
        menu.setName("敏感词规则");
        menu.setType("menu");
        menu.setParentId(contentRoot.getId());
        menu.setPathOrApi("/content/sensitive");
        menu.setSort(16);
        menu.setVisible(1);
        menus.add(menu);
    }
}
