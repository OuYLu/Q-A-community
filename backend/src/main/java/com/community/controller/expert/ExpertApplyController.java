package com.community.controller.expert;

import com.community.common.Result;
import com.community.dto.ExpertApplyDTO;
import com.community.service.ExpertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expert")
@RequiredArgsConstructor
@Tag(name = "专家认证申请")
public class ExpertApplyController {
    private final ExpertService expertService;

    @PostMapping("/apply")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "申请专家资质", description = "需要登录")
    public Result<Long> apply(@Valid @RequestBody ExpertApplyDTO dto) {
        return Result.success(expertService.applyExpert(dto));
    }
}
