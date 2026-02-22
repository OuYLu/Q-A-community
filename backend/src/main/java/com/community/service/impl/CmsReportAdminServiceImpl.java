package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.common.SecurityUser;
import com.community.dto.CmsReportHandleDTO;
import com.community.dto.CmsReportPageQueryDTO;
import com.community.entity.CmsAudit;
import com.community.entity.CmsReport;
import com.community.entity.KbEntry;
import com.community.entity.NotifyMessage;
import com.community.entity.QaAnswer;
import com.community.entity.QaComment;
import com.community.entity.QaQuestion;
import com.community.entity.User;
import com.community.mapper.CmsAuditMapper;
import com.community.mapper.CmsReportMapper;
import com.community.mapper.KbEntryMapper;
import com.community.mapper.NotifyMessageMapper;
import com.community.mapper.QaAnswerMapper;
import com.community.mapper.QaCommentMapper;
import com.community.mapper.QaQuestionMapper;
import com.community.mapper.UserMapper;
import com.community.service.CmsReportAdminService;
import com.community.vo.CmsReportAuthorVO;
import com.community.vo.CmsReportContentVO;
import com.community.vo.CmsReportDetailRowVO;
import com.community.vo.CmsReportDetailVO;
import com.community.vo.CmsReportPageItemVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CmsReportAdminServiceImpl implements CmsReportAdminService {
    private final CmsReportMapper cmsReportMapper;
    private final CmsAuditMapper cmsAuditMapper;
    private final QaQuestionMapper qaQuestionMapper;
    private final QaAnswerMapper qaAnswerMapper;
    private final QaCommentMapper qaCommentMapper;
    private final KbEntryMapper kbEntryMapper;
    private final UserMapper userMapper;
    private final NotifyMessageMapper notifyMessageMapper;

    @Override
    public PageInfo<CmsReportPageItemVO> page(CmsReportPageQueryDTO query) {
        int page = query == null || query.getPage() == null ? 1 : query.getPage();
        int pageSize = query == null || query.getPageSize() == null ? 10 : query.getPageSize();
        Integer status = query == null ? 1 : (query.getStatus() == null ? 1 : query.getStatus());
        PageHelper.startPage(page, pageSize);
        return new PageInfo<>(cmsReportMapper.selectAdminReportPage(
            query == null ? null : query.getBizType(),
            status,
            query == null ? null : query.getReasonType(),
            query == null ? null : query.getKeyword(),
            query == null ? null : query.getStartTime(),
            query == null ? null : query.getEndTime()
        ));
    }

    @Override
    public CmsReportDetailVO detail(Long id) {
        CmsReportDetailRowVO row = cmsReportMapper.selectAdminReportDetail(id);
        if (row == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "report not found");
        }
        CmsReportDetailVO vo = new CmsReportDetailVO();
        CmsReport report = new CmsReport();
        report.setId(row.getId());
        report.setBizType(row.getBizType());
        report.setBizId(row.getBizId());
        report.setReasonType(row.getReasonType());
        report.setReporterId(row.getReporterId());
        report.setReasonCode(row.getReasonCode());
        report.setReasonDetail(row.getReasonDetail());
        report.setStatus(row.getStatus());
        report.setHandlerId(row.getHandlerId());
        report.setHandleAction(row.getHandleAction());
        report.setHandleResult(row.getHandleResult());
        report.setHandledAt(row.getHandledAt());
        report.setCreatedAt(row.getCreatedAt());
        report.setUpdatedAt(row.getUpdatedAt());
        vo.setReport(report);

        CmsReportContentVO content = new CmsReportContentVO();
        content.setBizType(row.getBizType());
        content.setBizId(row.getBizId());
        content.setTitle(row.getContentTitle());
        content.setContent(row.getContentText());
        content.setStatus(row.getContentStatus());
        content.setRejectReason(row.getContentRejectReason());
        content.setCreatedAt(row.getContentCreatedAt());
        vo.setContent(content);

        CmsReportAuthorVO author = new CmsReportAuthorVO();
        author.setId(row.getAuthorId());
        author.setUsername(row.getAuthorUsername());
        author.setNickname(row.getAuthorNickname());
        author.setStatus(row.getAuthorStatus());
        vo.setAuthor(author);
        return vo;
    }

    @Override
    @Transactional
    public void handle(Long id, CmsReportHandleDTO dto) {
        CmsReport report = getReportOrThrow(id);
        if (report.getStatus() == null || report.getStatus() != 1) {
            throw new BizException(ResultCode.BAD_REQUEST, "only pending report can be handled");
        }
        Integer action = dto.getHandleAction();
        Long operatorId = currentUserId();
        report.setHandleAction(action);
        report.setHandleResult(dto.getHandleResult());
        report.setHandlerId(operatorId);
        report.setHandledAt(LocalDateTime.now());

        switch (action) {
            case 1 -> {
                updateTargetContentStatus(report.getBizType(), report.getBizId(), 4);
                report.setStatus(2);
            }
            case 2 -> {
                Long authorId = resolveContentAuthorId(report.getBizType(), report.getBizId());
                createWarningNotify(authorId, report);
                report.setStatus(2);
            }
            case 3 -> {
                Long authorId = resolveContentAuthorId(report.getBizType(), report.getBizId());
                User author = userMapper.selectById(authorId);
                if (author == null) {
                    throw new BizException(ResultCode.BAD_REQUEST, "target author not found");
                }
                author.setStatus(0);
                userMapper.updateById(author);
                report.setStatus(2);
            }
            case 4 -> report.setStatus(3);
            default -> throw new BizException(ResultCode.BAD_REQUEST, "unsupported handleAction");
        }
        cmsReportMapper.updateById(report);
    }

    @Override
    @Transactional
    public void toAudit(Long id) {
        CmsReport report = getReportOrThrow(id);
        CmsAudit exists = cmsAuditMapper.selectOne(new LambdaQueryWrapper<CmsAudit>()
            .eq(CmsAudit::getBizType, report.getBizType())
            .eq(CmsAudit::getBizId, report.getBizId())
            .eq(CmsAudit::getTriggerSource, 2)
            .eq(CmsAudit::getAuditStatus, 1)
            .last("LIMIT 1"));
        if (exists != null) {
            return;
        }
        CmsAudit audit = new CmsAudit();
        audit.setBizType(report.getBizType());
        audit.setBizId(report.getBizId());
        audit.setTriggerSource(2);
        audit.setAuditType(2);
        audit.setAuditStatus(1);
        audit.setAction(null);
        audit.setSubmitUserId(resolveContentAuthorId(report.getBizType(), report.getBizId()));
        cmsAuditMapper.insert(audit);
    }

    private CmsReport getReportOrThrow(Long id) {
        CmsReport report = cmsReportMapper.selectById(id);
        if (report == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "report not found");
        }
        return report;
    }

    private void updateTargetContentStatus(Integer bizType, Long bizId, int status) {
        switch (bizType) {
            case 1 -> {
                QaQuestion q = qaQuestionMapper.selectById(bizId);
                if (q == null) {
                    throw new BizException(ResultCode.BAD_REQUEST, "target question not found");
                }
                q.setStatus(status);
                qaQuestionMapper.updateById(q);
            }
            case 2 -> {
                QaAnswer a = qaAnswerMapper.selectById(bizId);
                if (a == null) {
                    throw new BizException(ResultCode.BAD_REQUEST, "target answer not found");
                }
                a.setStatus(status);
                qaAnswerMapper.updateById(a);
            }
            case 3 -> {
                QaComment c = qaCommentMapper.selectById(bizId);
                if (c == null) {
                    throw new BizException(ResultCode.BAD_REQUEST, "target comment not found");
                }
                c.setStatus(status);
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
            default -> throw new BizException(ResultCode.BAD_REQUEST, "unsupported bizType");
        }
    }

    private Long resolveContentAuthorId(Integer bizType, Long bizId) {
        return switch (bizType) {
            case 1 -> {
                QaQuestion q = qaQuestionMapper.selectById(bizId);
                if (q == null) {
                    throw new BizException(ResultCode.BAD_REQUEST, "target question not found");
                }
                yield q.getUserId();
            }
            case 2 -> {
                QaAnswer a = qaAnswerMapper.selectById(bizId);
                if (a == null) {
                    throw new BizException(ResultCode.BAD_REQUEST, "target answer not found");
                }
                yield a.getUserId();
            }
            case 3 -> {
                QaComment c = qaCommentMapper.selectById(bizId);
                if (c == null) {
                    throw new BizException(ResultCode.BAD_REQUEST, "target comment not found");
                }
                yield c.getUserId();
            }
            case 4 -> {
                KbEntry e = kbEntryMapper.selectById(bizId);
                if (e == null) {
                    throw new BizException(ResultCode.BAD_REQUEST, "target kb entry not found");
                }
                yield e.getAuthorUserId();
            }
            default -> throw new BizException(ResultCode.BAD_REQUEST, "unsupported bizType");
        };
    }

    private void createWarningNotify(Long receiverId, CmsReport report) {
        if (receiverId == null) {
            return;
        }
        NotifyMessage notify = new NotifyMessage();
        notify.setReceiverId(receiverId);
        notify.setType(1);
        notify.setBizType(report.getBizType());
        notify.setBizId(report.getBizId());
        notify.setTitle("内容警告");
        notify.setContent("你的内容收到举报，请注意社区规范。");
        notify.setIsRead(0);
        notifyMessageMapper.insert(notify);
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof SecurityUser su)) {
            return null;
        }
        return su.getId();
    }
}
