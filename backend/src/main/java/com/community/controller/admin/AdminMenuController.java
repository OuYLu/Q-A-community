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
@Tag(name = "Admin Menu")
public class AdminMenuController {
    private final MenuService menuService;

    @GetMapping("/menu/list")
    @Operation(summary = "Admin menu list", description = "Dynamic menu list for current user")
    public Result<List<AdminMenuVO>> listMenus() {
        return Result.success(menuService.listCurrentUserMenus());
    }
}
