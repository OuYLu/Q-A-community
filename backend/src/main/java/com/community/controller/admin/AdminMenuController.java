package com.community.controller.admin;

import com.community.common.Result;
import com.community.service.MenuService;
import com.community.vo.AdminMenuVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "后台菜单管理")
public class AdminMenuController {
    private final MenuService menuService;

    @GetMapping("/menu/list")
    @Operation(summary = "后台菜单列表", description = "当前用户动态菜单列表")
    public Result<List<AdminMenuVO>> listMenus() {
        return Result.success(menuService.listCurrentUserMenus());
    }
}
