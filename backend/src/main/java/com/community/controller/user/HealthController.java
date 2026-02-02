package com.community.controller.user;

import com.community.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "用户端-健康检查")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "健康检查")
    public Result<String> health() {
        return Result.success("ok");
    }
}
