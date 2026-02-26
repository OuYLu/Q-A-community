package com.community.controller.customer;

import com.community.common.Result;
import com.community.dto.AppSearchLogDTO;
import com.community.dto.AppSearchQueryDTO;
import com.community.service.CustomerSearchService;
import com.community.vo.AppSearchHistoryVO;
import com.community.vo.AppSearchHotVO;
import com.community.vo.AppSearchResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer/search")
@RequiredArgsConstructor
@Tag(name = "客户端搜索")
public class CustomerSearchController {
    private final CustomerSearchService customerSearchService;

    @GetMapping
    @Operation(summary = "搜索")
    public Result<AppSearchResultVO> search(@ModelAttribute AppSearchQueryDTO query) {
        return Result.success(customerSearchService.search(query));
    }

    @GetMapping("/hot")
    @Operation(summary = "热搜")
    public Result<List<AppSearchHotVO>> hot(@RequestParam(required = false) Integer limit) {
        return Result.success(customerSearchService.hot(limit));
    }

    @GetMapping("/history")
    @Operation(summary = "搜索历史")
    public Result<List<AppSearchHistoryVO>> history(@RequestParam(required = false) Integer limit) {
        return Result.success(customerSearchService.history(limit));
    }

    @DeleteMapping("/history")
    @Operation(summary = "清空搜索历史")
    public Result<Void> clearHistory() {
        customerSearchService.clearHistory();
        return Result.success(null);
    }

    @PostMapping("/log")
    @Operation(summary = "记录搜索")
    public Result<Void> log(@Valid @RequestBody AppSearchLogDTO dto) {
        customerSearchService.logSearch(dto);
        return Result.success(null);
    }
}



