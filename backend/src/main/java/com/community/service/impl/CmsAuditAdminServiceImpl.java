package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.common.SecurityUser;
import com.community.dto.CmsAuditBatchReviewDTO;
import com.community.dto.CmsAuditPageQueryDTO;
import com.community.dto.CmsAuditReviewDTO;
import com.community.entity.CmsAudit;
import com.community.entity.KbEntry;
import com.community.entity.QaAnswer;
import com.community.entity.QaComment;
import com.community.entity.QaQuestion;
import com.community.mapper.CmsAuditMapper;
import com.community.mapper.KbEntryMapper;
import com.community.mapper.QaAnswerMapper;
import com.community.mapper.QaCommentMapper;
import com.community.mapper.QaQuestionMapper;
import com.community.service.CmsAuditAdminService;
import com.community.vo.CmsAuditAuthorVO;
import com.community.vo.CmsAuditContentVO;
import com.community.vo.CmsAuditDetailRowVO;
import com.community.vo.CmsAuditDetailVO;
import com.community.vo.CmsAuditPageItemVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CmsAuditAdminServiceImpl implements CmsAuditAdminService {
    private final CmsAuditMapper cmsAuditMapper;
    private final QaQuestionMapper qaQuestionMapper;
    private final QaAnswerMapper qaAnswerMapper;
    private final QaCommentMapper qaCommentMapper;
    private final KbEntryMapper kbEntryMapper;

    @Override
    public PageInfo<CmsAuditPageItemVO> page(CmsAuditPageQueryDTO query) {
        int page = query == null || query.getPage() == null ? 1 : query.getPage();
        int pageSize = query == null || query.getPageSize() == null ? 10 : query.getPageSize();
        Integer auditStatus = query == null ? 1 : (query.getAuditStatus() == null ? 1 : query.getAuditStatus());
        PageHelper.startPage(page, pageSize);
        return new PageInfo<>(cmsAuditMapper.selectAdminAuditPage(
            query == null ? null : query.getBizType(),
            auditStatus,
            query == null ? null : query.getTriggerSource(),
            query == null ? null : query.getKeyword(),
            query == null ? null : query.getStartTime(),
            query == null ? null : query.getEndTime(),
            query == null ? null : query.getSortBy(),
            query == null ? null : query.getSortOrder()
        ));
    }

    @Override
    public CmsAuditDetailVO detail(Long id) {
        CmsAuditDetailRowVO row = cmsAuditMapper.selectAdminAuditDetail(id);
        if (row == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "audit not found");
        }
        CmsAuditDetailVO vo = new CmsAuditDetailVO();
        CmsAudit audit = new CmsAudit();
        audit.setId(row.getId());
        audit.setBizType(row.getBizType());
        audit.setBizId(row.getBizId());
        audit.setTriggerSource(row.getTriggerSource());
        audit.setAuditType(row.getAuditType());
        audit.setAuditStatus(row.getAuditStatus());
        audit.setAction(row.getAction());
        audit.setModelLabel(row.getModelLabel());
        audit.setModelScore(row.getModelScore());
        audit.setHitDetail(row.getHitDetail());
        audit.setRejectReason(row.getRejectReason());
        audit.setSubmitUserId(row.getSubmitUserId());
        audit.setAuditorId(row.getAuditorId());
        audit.setAuditedAt(row.getAuditedAt());
        audit.setCreatedAt(row.getCreatedAt());
        audit.setUpdatedAt(row.getUpdatedAt());
        vo.setAudit(audit);

        CmsAuditContentVO content = new CmsAuditContentVO();
        content.setBizType(row.getBizType());
        content.setBizId(row.getBizId());
        content.setTitle(row.getContentTitle());
        content.setContent(row.getContentText());
        content.setStatus(row.getContentStatus());
        content.setRejectReason(row.getContentRejectReason());
        content.setCreatedAt(row.getContentCreatedAt());
        vo.setContent(content);

        CmsAuditAuthorVO author = new CmsAuditAuthorVO();
        author.setId(row.getAuthorId());
        author.setUsername(row.getAuthorUsername());
        author.setNickname(row.getAuthorNickname());
        author.setStatus(row.getAuthorStatus());
        vo.setAuthor(author);
        return vo;
    }

    @Override
    @Transactional
    public void review(Long id, CmsAuditReviewDTO dto) {
        CmsAudit audit = getAuditOrThrow(id);
        if (audit.getAuditStatus() == null || audit.getAuditStatus() != 1) {
            throw new BizException(ResultCode.BAD_REQUEST, "only pending audit can be reviewed");
        }
        String action = normalizeAction(dto.getAction());
        if ("reject".equals(action) && (dto.getRejectReason() == null || dto.getRejectReason().isBlank())) {
            throw new BizException(ResultCode.BAD_REQUEST, "rejectReason is required when action=reject");
        }
        applyReview(audit, action, dto.getRejectReason());
    }

    @Override
    @Transactional
    public void batchReview(CmsAuditBatchReviewDTO dto) {
        String action = normalizeAction(dto.getAction());
        if ("reject".equals(action) && (dto.getRejectReason() == null || dto.getRejectReason().isBlank())) {
            throw new BizException(ResultCode.BAD_REQUEST, "rejectReason is required when action=reject");
        }
        List<Long> ids = dto.getIds().stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "ids cannot be empty");
        }
        List<CmsAudit> audits = cmsAuditMapper.selectList(new LambdaQueryWrapper<CmsAudit>()
            .in(CmsAudit::getId, ids));
        if (audits.size() != ids.size()) {
            throw new BizException(ResultCode.BAD_REQUEST, "contains invalid audit id");
        }
        for (CmsAudit audit : audits) {
            if (audit.getAuditStatus() == null || audit.getAuditStatus() != 1) {
                throw new BizException(ResultCode.BAD_REQUEST, "batch review contains non-pending audit");
            }
        }
        for (CmsAudit audit : audits) {
            applyReview(audit, action, dto.getRejectReason());
        }
    }

    @Override
    @Transactional
    public void reopen(Long id) {
        CmsAudit audit = getAuditOrThrow(id);
        audit.setAuditStatus(1);
        audit.setAction(null);
        audit.setRejectReason(null);
        audit.setAuditorId(null);
        audit.setAuditedAt(null);
        cmsAuditMapper.updateById(audit);
        updateTargetContentStatus(audit.getBizType(), audit.getBizId(), 2, null);
    }

    private void applyReview(CmsAudit audit, String action, String rejectReason) {
        boolean pass = "pass".equals(action);
        audit.setAuditStatus(pass ? 2 : 3);
        audit.setAction(action);
        audit.setRejectReason(pass ? null : rejectReason);
        audit.setAuditorId(currentUserId());
        audit.setAuditedAt(LocalDateTime.now());
        cmsAuditMapper.updateById(audit);
        updateTargetContentStatus(audit.getBizType(), audit.getBizId(), pass ? 1 : 3, pass ? null : rejectReason);
    }

    private String normalizeAction(String raw) {
        String action = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        if (!"pass".equals(action) && !"reject".equals(action)) {
            throw new BizException(ResultCode.BAD_REQUEST, "action must be pass or reject");
        }
        return action;
    }

    private CmsAudit getAuditOrThrow(Long id) {
        CmsAudit audit = cmsAuditMapper.selectById(id);
        if (audit == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "audit not found");
        }
        return audit;
    }

    private void updateTargetContentStatus(Integer bizType, Long bizId, int status, String rejectReason) {
        switch (bizType) {
            case 1 -> {
                QaQuestion q = qaQuestionMapper.selectById(bizId);
                if (q == null) {
                    throw new BizException(ResultCode.BAD_REQUEST, "target question not found");
                }
                q.setStatus(status);
                q.setRejectReason(rejectReason);
                qaQuestionMapper.updateById(q);
            }
            case 2 -> {
                QaAnswer a = qaAnswerMapper.selectById(bizId);
                if (a == null) {
                    throw new BizException(ResultCode.BAD_REQUEST, "target answer not found");
                }
                a.setStatus(status);
                a.setRejectReason(rejectReason);
                qaAnswerMapper.updateById(a);
            }
            case 3 -> {
                QaComment c = qaCommentMapper.selectById(bizId);
                if (c == null) {
                    throw new BizException(ResultCode.BAD_REQUEST, "target comment not found");
                }
                c.setStatus(status);
                c.setRejectReason(rejectReason);
                qaCommentMapper.updateById(c);
            }
            case 4 -> {
                KbEntry e = kbEntryMapper.selectById(bizId);
                if (e == null) {
                    throw new BizException(ResultCode.BAD_REQUEST, "target kb entry not found");
                }
                e.setStatus(status);
                kbEntryMapper.updateById(e);
            }
            default -> throw new BizException(ResultCode.BAD_REQUEST, "unsupported bizType: " + bizType);
        }
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof SecurityUser su)) {
            return null;
        }
        return su.getId();
    }
}
