package com.community.controller.customer;

import com.community.common.Result;
import com.community.dto.AppAnswerCreateDTO;
import com.community.dto.AppAnswerCommentCreateDTO;
import com.community.dto.AppAnswerUpdateDTO;
import com.community.dto.AppPageQueryDTO;
import com.community.dto.AppQuestionCreateDTO;
import com.community.dto.AppQuestionPageQueryDTO;
import com.community.dto.AppQuestionUpdateDTO;
import com.community.service.CustomerQuestionService;
import com.community.vo.AppMyQuestionItemVO;
import com.community.vo.AppAnswerDetailVO;
import com.community.vo.AppAnswerCommentVO;
import com.community.vo.AppQuestionDetailVO;
import com.community.vo.AppQuestionListItemVO;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Customer Question APIs")
public class CustomerQuestionController {
    private final CustomerQuestionService customerQuestionService;

    @GetMapping("/api/customer/questions")
    @Operation(summary = "Question page")
    public Result<PageInfo<AppQuestionListItemVO>> page(@ModelAttribute AppQuestionPageQueryDTO query) {
        return Result.success(customerQuestionService.page(query));
    }

    @GetMapping("/api/customer/questions/my")
    @Operation(summary = "My questions")
    public Result<PageInfo<AppMyQuestionItemVO>> myQuestions(@ModelAttribute AppPageQueryDTO query) {
        return Result.success(customerQuestionService.myQuestions(query));
    }

    @GetMapping("/api/customer/questions/{id}")
    @Operation(summary = "Question detail")
    public Result<AppQuestionDetailVO> detail(@PathVariable Long id) {
        return Result.success(customerQuestionService.detail(id));
    }

    @PostMapping("/api/customer/questions")
    @Operation(summary = "Create question")
    public Result<Long> createQuestion(@Valid @RequestBody AppQuestionCreateDTO dto) {
        return Result.success(customerQuestionService.createQuestion(dto));
    }

    @PutMapping("/api/customer/questions/{id}")
    @Operation(summary = "Update question")
    public Result<Void> updateQuestion(@PathVariable Long id, @Valid @RequestBody AppQuestionUpdateDTO dto) {
        customerQuestionService.updateQuestion(id, dto);
        return Result.success(null);
    }

    @DeleteMapping("/api/customer/questions/{id}")
    @Operation(summary = "Delete question")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        customerQuestionService.deleteQuestion(id);
        return Result.success(null);
    }

    @PostMapping("/api/customer/questions/{id}/answers")
    @Operation(summary = "Create answer")
    public Result<Long> createAnswer(@PathVariable Long id, @Valid @RequestBody AppAnswerCreateDTO dto) {
        return Result.success(customerQuestionService.createAnswer(id, dto));
    }

    @PutMapping("/api/customer/answers/{id}")
    @Operation(summary = "Update answer")
    public Result<Void> updateAnswer(@PathVariable Long id, @Valid @RequestBody AppAnswerUpdateDTO dto) {
        customerQuestionService.updateAnswer(id, dto);
        return Result.success(null);
    }

    @DeleteMapping("/api/customer/answers/{id}")
    @Operation(summary = "Delete answer")
    public Result<Void> deleteAnswer(@PathVariable Long id) {
        customerQuestionService.deleteAnswer(id);
        return Result.success(null);
    }

    @PostMapping("/api/customer/questions/{id}/like")
    @Operation(summary = "Toggle question like")
    public Result<AppQuestionDetailVO> toggleLike(@PathVariable Long id) {
        return Result.success(customerQuestionService.toggleQuestionLike(id));
    }

    @PostMapping("/api/customer/questions/{id}/favorite")
    @Operation(summary = "Toggle question favorite")
    public Result<AppQuestionDetailVO> toggleFavorite(@PathVariable Long id) {
        return Result.success(customerQuestionService.toggleQuestionFavorite(id));
    }

    @GetMapping("/api/customer/answers/{id}")
    @Operation(summary = "Answer detail")
    public Result<AppAnswerDetailVO> answerDetail(@PathVariable Long id) {
        return Result.success(customerQuestionService.answerDetail(id));
    }

    @PostMapping("/api/customer/answers/{id}/like")
    @Operation(summary = "Toggle answer like")
    public Result<AppAnswerDetailVO> toggleAnswerLike(@PathVariable Long id) {
        return Result.success(customerQuestionService.toggleAnswerLike(id));
    }

    @PostMapping("/api/customer/answers/{id}/favorite")
    @Operation(summary = "Toggle answer favorite")
    public Result<AppAnswerDetailVO> toggleAnswerFavorite(@PathVariable Long id) {
        return Result.success(customerQuestionService.toggleAnswerFavorite(id));
    }

    @PostMapping("/api/customer/questions/{questionId}/answers/{answerId}/recommend")
    @Operation(summary = "Recommend answer as best")
    public Result<Void> recommendBest(@PathVariable Long questionId, @PathVariable Long answerId) {
        customerQuestionService.recommendBestAnswer(questionId, answerId);
        return Result.success(null);
    }

    @GetMapping("/api/customer/answers/{id}/comments")
    @Operation(summary = "Answer comment list")
    public Result<List<AppAnswerCommentVO>> answerComments(@PathVariable Long id) {
        return Result.success(customerQuestionService.answerComments(id));
    }

    @PostMapping("/api/customer/answers/{id}/comments")
    @Operation(summary = "Create answer comment")
    public Result<Long> createAnswerComment(@PathVariable Long id, @Valid @RequestBody AppAnswerCommentCreateDTO dto) {
        return Result.success(customerQuestionService.createAnswerComment(id, dto));
    }
}
