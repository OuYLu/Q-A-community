package com.community.controller.admin;

import com.community.common.Result;
import com.community.dto.QaManageAnswerPageQueryDTO;
import com.community.dto.QaManageQuestionPageQueryDTO;
import com.community.dto.QaManageStatusUpdateDTO;
import com.community.service.QaManageAdminService;
import com.community.vo.AdminQaAnswerPageItemVO;
import com.community.vo.AdminQaQuestionPageItemVO;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/qa/manage")
@RequiredArgsConstructor
@Tag(name = "后台问答管理")
public class AdminQaManageController {
    private static final String QA_MANAGE_AUTH = "hasAnyAuthority("
        + "'menu:content:qa',"
        + "'content:qa:manage',"
        + "'op:qa:manage',"
        + "'menu:content:audit',"
        + "'menu:content:report',"
        + "'content:audit:manage',"
        + "'content:report:manage',"
        + "'op:audit:manage',"
        + "'op:report:manage'"
        + ")";

    private final QaManageAdminService qaManageAdminService;

    @GetMapping("/question/page")
    @PreAuthorize(QA_MANAGE_AUTH)
    @Operation(summary = "问答管理-问题分页")
    public Result<PageInfo<AdminQaQuestionPageItemVO>> questionPage(@ModelAttribute QaManageQuestionPageQueryDTO query) {
        return Result.success(qaManageAdminService.questionPage(query));
    }

    @GetMapping("/answer/page")
    @PreAuthorize(QA_MANAGE_AUTH)
    @Operation(summary = "问答管理-回答分页")
    public Result<PageInfo<AdminQaAnswerPageItemVO>> answerPage(@ModelAttribute QaManageAnswerPageQueryDTO query) {
        return Result.success(qaManageAdminService.answerPage(query));
    }

    @PutMapping("/question/{id}/status")
    @PreAuthorize(QA_MANAGE_AUTH)
    @Operation(summary = "问答管理-修改问题状态")
    public Result<Void> updateQuestionStatus(@PathVariable Long id, @Valid @RequestBody QaManageStatusUpdateDTO dto) {
        qaManageAdminService.updateQuestionStatus(id, dto);
        return Result.success(null);
    }

    @PutMapping("/answer/{id}/status")
    @PreAuthorize(QA_MANAGE_AUTH)
    @Operation(summary = "问答管理-修改回答状态")
    public Result<Void> updateAnswerStatus(@PathVariable Long id, @Valid @RequestBody QaManageStatusUpdateDTO dto) {
        qaManageAdminService.updateAnswerStatus(id, dto);
        return Result.success(null);
    }

    @DeleteMapping("/question/{id}")
    @PreAuthorize(QA_MANAGE_AUTH)
    @Operation(summary = "问答管理-删除问题")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        qaManageAdminService.deleteQuestion(id);
        return Result.success(null);
    }

    @DeleteMapping("/answer/{id}")
    @PreAuthorize(QA_MANAGE_AUTH)
    @Operation(summary = "问答管理-删除回答")
    public Result<Void> deleteAnswer(@PathVariable Long id) {
        qaManageAdminService.deleteAnswer(id);
        return Result.success(null);
    }
}

