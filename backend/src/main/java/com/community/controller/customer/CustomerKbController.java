package com.community.controller.customer;

import com.community.common.Result;
import com.community.dto.AppAnswerCommentCreateDTO;
import com.community.service.CustomerKbService;
import com.community.vo.AppKbCommentVO;
import com.community.vo.AppKbInteractVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Customer KB APIs")
public class CustomerKbController {
    private final CustomerKbService customerKbService;

    @GetMapping("/api/customer/kb/{id}/interaction")
    @Operation(summary = "KB interaction summary")
    public Result<AppKbInteractVO> interaction(@PathVariable Long id) {
        return Result.success(customerKbService.interaction(id));
    }

    @PostMapping("/api/customer/kb/{id}/like")
    @Operation(summary = "Toggle KB like")
    public Result<AppKbInteractVO> toggleLike(@PathVariable Long id) {
        return Result.success(customerKbService.toggleLike(id));
    }

    @PostMapping("/api/customer/kb/{id}/favorite")
    @Operation(summary = "Toggle KB favorite")
    public Result<AppKbInteractVO> toggleFavorite(@PathVariable Long id) {
        return Result.success(customerKbService.toggleFavorite(id));
    }

    @GetMapping("/api/customer/kb/{id}/comments")
    @Operation(summary = "KB comment list")
    public Result<List<AppKbCommentVO>> comments(@PathVariable Long id) {
        return Result.success(customerKbService.comments(id));
    }

    @PostMapping("/api/customer/kb/{id}/comments")
    @Operation(summary = "Create KB comment")
    public Result<Long> createComment(@PathVariable Long id, @Valid @RequestBody AppAnswerCommentCreateDTO dto) {
        return Result.success(customerKbService.createComment(id, dto));
    }
}
