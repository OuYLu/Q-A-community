package com.community.controller;

import com.community.common.annotation.RequirePermission;
import com.community.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RbacPingController {

    @GetMapping("/admin/ping")
    @RequirePermission("admin:ping")
    public Result<String> adminPing() {
        return Result.success("ok");
    }

    @GetMapping("/user/ping")
    @RequirePermission("user:ping")
    public Result<String> userPing() {
        return Result.success("ok");
    }
}
