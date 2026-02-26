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

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {
    private final PermissionMapper permissionMapper;

    @Override
    public List<AdminMenuVO> listCurrentUserMenus() {
        SecurityUser securityUser = getCurrentSecurityUser();
        if (securityUser == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "请先登录");
        }

        List<Permission> menus = permissionMapper.selectMenusByUserId(securityUser.getId());
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
}
