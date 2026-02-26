package com.community.controller.customer;

import com.community.common.Result;
import com.community.dto.CustomerRegisterDTO;
import com.community.service.UserService;
import com.community.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
@Tag(name = "客户端-注册")
public class CustomerRegisterController {
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "客户注册", description = "仅客户注册入口，默认分配客户角色")
    public Result<UserVO> register(@Valid @RequestBody CustomerRegisterDTO dto) {
        return Result.success(userService.registerCustomer(dto));
    }
}
