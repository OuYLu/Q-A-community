package com.community.controller.customer;

import com.community.common.Result;
import com.community.dto.AppNotificationPageQueryDTO;
import com.community.service.CustomerNotificationService;
import com.community.vo.AppNotificationItemVO;
import com.community.vo.AppNotificationUnreadCountVO;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer/notifications")
@RequiredArgsConstructor
@Tag(name = "客户端通知")
public class CustomerNotificationController {
    private final CustomerNotificationService customerNotificationService;

    @GetMapping
    @Operation(summary = "通知分页")
    public Result<PageInfo<AppNotificationItemVO>> page(@ModelAttribute AppNotificationPageQueryDTO query) {
        return Result.success(customerNotificationService.page(query));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "未读数量")
    public Result<AppNotificationUnreadCountVO> unreadCount() {
        return Result.success(customerNotificationService.unreadCount());
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "读取单条")
    public Result<Void> readOne(@PathVariable Long id) {
        customerNotificationService.readOne(id);
        return Result.success(null);
    }

    @PostMapping("/read-all")
    @Operation(summary = "全部已读")
    public Result<Void> readAll() {
        customerNotificationService.readAll();
        return Result.success(null);
    }
}

