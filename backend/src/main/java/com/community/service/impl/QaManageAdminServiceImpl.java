package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.dto.QaManageAnswerPageQueryDTO;
import com.community.dto.QaManageQuestionPageQueryDTO;
import com.community.dto.QaManageStatusUpdateDTO;
import com.community.entity.QaAnswer;
import com.community.entity.QaQuestion;
import com.community.mapper.QaAnswerMapper;
import com.community.mapper.QaQuestionMapper;
import com.community.service.EsSearchService;
import com.community.service.QaManageAdminService;
import com.community.vo.AdminQaAnswerPageItemVO;
import com.community.vo.AdminQaQuestionPageItemVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class QaManageAdminServiceImpl implements QaManageAdminService {
    private static final Set<Integer> MANAGE_STATUS = Set.of(1, 4);

    private final QaQuestionMapper qaQuestionMapper;
    private final QaAnswerMapper qaAnswerMapper;
    private final EsSearchService esSearchService;

    @Override
    public PageInfo<AdminQaQuestionPageItemVO> questionPage(QaManageQuestionPageQueryDTO query) {
        int page = query == null || query.getPage() == null || query.getPage() <= 0 ? 1 : query.getPage();
        int pageSize = query == null || query.getPageSize() == null || query.getPageSize() <= 0 ? 10 : query.getPageSize();
        Integer status = query == null ? null : query.getStatus();
        if (status != null && status == QaQuestion.STATUS_SELF_ONLY) {
            status = null;
        }
        PageHelper.startPage(page, Math.min(pageSize, 100));
        return new PageInfo<>(qaQuestionMapper.selectAdminQaQuestionPage(
            query == null ? null : query.getKeyword(),
            status,
            query == null ? 0 : query.getDeleteFlag(),
            query == null ? null : query.getCategoryId(),
            query == null ? null : query.getTopicId(),
            query == null ? null : query.getUserId(),
            query == null ? null : query.getStartTime(),
            query == null ? null : query.getEndTime(),
            query == null ? null : query.getSortBy(),
            query == null ? null : query.getSortOrder()
        ));
    }

    @Override
    public PageInfo<AdminQaAnswerPageItemVO> answerPage(QaManageAnswerPageQueryDTO query) {
        int page = query == null || query.getPage() == null || query.getPage() <= 0 ? 1 : query.getPage();
        int pageSize = query == null || query.getPageSize() == null || query.getPageSize() <= 0 ? 10 : query.getPageSize();
        PageHelper.startPage(page, Math.min(pageSize, 100));
        return new PageInfo<>(qaAnswerMapper.selectAdminQaAnswerPage(
            query == null ? null : query.getKeyword(),
            query == null ? null : query.getStatus(),
            query == null ? 0 : query.getDeleteFlag(),
            query == null ? null : query.getQuestionId(),
            query == null ? null : query.getUserId(),
            query == null ? null : query.getStartTime(),
            query == null ? null : query.getEndTime(),
            query == null ? null : query.getSortBy(),
            query == null ? null : query.getSortOrder()
        ));
    }

    @Override
    @Transactional
    public void updateQuestionStatus(Long id, QaManageStatusUpdateDTO dto) {
        QaQuestion question = requireQuestion(id);
        Integer target = resolveStatus(dto);
        if (question.getDeleteFlag() != null && question.getDeleteFlag() == 1) {
            throw new BizException(ResultCode.BAD_REQUEST, "问题已删除，不能修改状态");
        }
        if (target.equals(question.getStatus())) {
            return;
        }
        question.setStatus(target);
        if (target == 4 && question.getAcceptedAnswerId() != null) {
            question.setAcceptedAnswerId(null);
            question.setAcceptedAt(null);
        }
        question.setUpdatedAt(LocalDateTime.now());
        qaQuestionMapper.updateById(question);
        syncQuestion(question.getId());
    }

    @Override
    @Transactional
    public void updateAnswerStatus(Long id, QaManageStatusUpdateDTO dto) {
        QaAnswer answer = requireAnswer(id);
        Integer target = resolveStatus(dto);
        if (answer.getDeleteFlag() != null && answer.getDeleteFlag() == 1) {
            throw new BizException(ResultCode.BAD_REQUEST, "回答已删除，不能修改状态");
        }
        Integer from = answer.getStatus() == null ? 1 : answer.getStatus();
        if (target.equals(from)) {
            return;
        }
        answer.setStatus(target);
        answer.setUpdatedAt(LocalDateTime.now());
        qaAnswerMapper.updateById(answer);

        if (from == 1 && target != 1) {
            qaQuestionMapper.updateAnswerCount(answer.getQuestionId(), -1);
            clearAcceptedIfNeeded(answer.getQuestionId(), answer.getId());
        } else if (from != 1 && target == 1) {
            qaQuestionMapper.updateAnswerCount(answer.getQuestionId(), 1);
        }
        syncQuestion(answer.getQuestionId());
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        QaQuestion question = requireQuestion(id);
        if (question.getDeleteFlag() != null && question.getDeleteFlag() == 1) {
            return;
        }
        question.setDeleteFlag(1);
        question.setStatus(QaQuestion.STATUS_OFFLINE);
        question.setAcceptedAnswerId(null);
        question.setAcceptedAt(null);
        question.setUpdatedAt(LocalDateTime.now());
        qaQuestionMapper.updateById(question);
        syncQuestion(question.getId());
    }

    @Override
    @Transactional
    public void deleteAnswer(Long id) {
        QaAnswer answer = requireAnswer(id);
        if (answer.getDeleteFlag() != null && answer.getDeleteFlag() == 1) {
            return;
        }
        boolean wasPublished = answer.getStatus() != null && answer.getStatus() == 1;
        answer.setDeleteFlag(1);
        answer.setStatus(4);
        answer.setUpdatedAt(LocalDateTime.now());
        qaAnswerMapper.updateById(answer);
        if (wasPublished) {
            qaQuestionMapper.updateAnswerCount(answer.getQuestionId(), -1);
        }
        clearAcceptedIfNeeded(answer.getQuestionId(), answer.getId());
        syncQuestion(answer.getQuestionId());
    }

    private Integer resolveStatus(QaManageStatusUpdateDTO dto) {
        if (dto == null || dto.getStatus() == null || !MANAGE_STATUS.contains(dto.getStatus())) {
            throw new BizException(ResultCode.BAD_REQUEST, "仅支持状态：1-发布，4-下架");
        }
        return dto.getStatus();
    }

    private QaQuestion requireQuestion(Long id) {
        QaQuestion question = qaQuestionMapper.selectById(id);
        if (question == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "问题不存在");
        }
        return question;
    }

    private QaAnswer requireAnswer(Long id) {
        QaAnswer answer = qaAnswerMapper.selectById(id);
        if (answer == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "回答不存在");
        }
        return answer;
    }

    private void clearAcceptedIfNeeded(Long questionId, Long answerId) {
        if (questionId == null || answerId == null) {
            return;
        }
        QaQuestion question = qaQuestionMapper.selectById(questionId);
        if (question == null || question.getAcceptedAnswerId() == null || !answerId.equals(question.getAcceptedAnswerId())) {
            return;
        }
        qaQuestionMapper.update(
            null,
            new LambdaUpdateWrapper<QaQuestion>()
                .eq(QaQuestion::getId, questionId)
                .set(QaQuestion::getAcceptedAnswerId, null)
                .set(QaQuestion::getAcceptedAt, null)
        );
    }

    private void syncQuestion(Long questionId) {
        if (esSearchService == null || !esSearchService.isEnabled() || questionId == null) {
            return;
        }
        esSearchService.syncQuestionById(questionId);
    }
}
