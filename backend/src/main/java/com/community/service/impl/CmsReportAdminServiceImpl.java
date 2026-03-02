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
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CmsReportAdminServiceImpl implements CmsReportAdminService {
    private static final int NOTIFY_TYPE_REPORT_FEEDBACK = 7;

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
            throw new BizException(ResultCode.BAD_REQUEST, "举报记录不存在");
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
        if (report.getStatus() == null || report.getStatus() != CmsReport.STATUS_PENDING) {
            throw new BizException(ResultCode.BAD_REQUEST, "仅待处理举报可执行处理");
        }

        Integer action = dto.getHandleAction();
        Long operatorId = currentUserId();
        Long reporterId = report.getReporterId();
        Long authorId = resolveContentAuthorId(report.getBizType(), report.getBizId());
        String contentTitle = resolveContentTitle(report.getBizType(), report.getBizId());

        report.setHandleAction(action);
        report.setHandleResult(dto.getHandleResult());
        report.setHandlerId(operatorId);
        report.setHandledAt(LocalDateTime.now());

        switch (action) {
            case CmsReport.HANDLE_ACTION_OFFLINE -> {
                updateTargetContentStatus(report.getBizType(), report.getBizId(), 4);
                report.setStatus(CmsReport.STATUS_HANDLED);
                createReportOutcomeNotifyToAuthor(authorId, report, contentTitle, "处理结果：下架");
                createReportOutcomeNotifyToReporter(reporterId, report, contentTitle, "处理结果：已下架");
            }
            case CmsReport.HANDLE_ACTION_WARN -> {
                report.setStatus(CmsReport.STATUS_HANDLED);
                createReportOutcomeNotifyToAuthor(authorId, report, contentTitle, "处理结果：警告，请注意内容规范");
                createReportOutcomeNotifyToReporter(reporterId, report, contentTitle, "处理结果：已警告");
            }
            case CmsReport.HANDLE_ACTION_BAN -> {
                updateTargetContentStatus(report.getBizType(), report.getBizId(), 4);
                User author = userMapper.selectById(authorId);
                if (author == null) {
                    throw new BizException(ResultCode.BAD_REQUEST, "被举报内容作者不存在");
                }
                int banDays = dto.getBanDays() == null || dto.getBanDays() <= 0 ? 3 : dto.getBanDays();
                author.setBanUntil(LocalDateTime.now().plusDays(banDays));
                author.setBanReason(StringUtils.hasText(dto.getHandleResult()) ? dto.getHandleResult().trim() : "违规内容封禁");
                userMapper.updateById(author);

                report.setStatus(CmsReport.STATUS_HANDLED);
                createReportOutcomeNotifyToAuthor(authorId, report, contentTitle, "处理结果：封禁 " + banDays + " 天并下架");
                createReportOutcomeNotifyToReporter(reporterId, report, contentTitle, "处理结果：已封禁并下架");
            }
            case CmsReport.HANDLE_ACTION_IGNORE -> {
                report.setStatus(CmsReport.STATUS_REJECTED);
                createReportOutcomeNotifyToReporter(reporterId, report, contentTitle, "处理结果：不予处理");
            }
            default -> throw new BizException(ResultCode.BAD_REQUEST, "不支持的处理动作");
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
            throw new BizException(ResultCode.BAD_REQUEST, "举报记录不存在");
        }
        return report;
    }

    private void updateTargetContentStatus(Integer bizType, Long bizId, int status) {
        switch (bizType) {
            case CmsReport.BIZ_TYPE_QUESTION -> {
                QaQuestion q = qaQuestionMapper.selectById(bizId);
                if (q == null) {
                    throw new BizException(ResultCode.BAD_REQUEST, "目标问题不存在");
                }
                q.setStatus(status);
                qaQuestionMapper.updateById(q);
            }
            case CmsReport.BIZ_TYPE_ANSWER -> {
                QaAnswer a = qaAnswerMapper.selectById(bizId);
                if (a == null) {
                    throw new BizException(ResultCode.BAD_REQUEST, "目标回答不存在");
                }
                a.setStatus(status);
                qaAnswerMapper.updateById(a);
            }
            case CmsReport.BIZ_TYPE_COMMENT -> {
                QaComment c = qaCommentMapper.selectById(bizId);
                if (c == null) {
                    throw new BizException(ResultCode.BAD_REQUEST, "目标评论不存在");
                }
                c.setStatus(status);
                qaCommentMapper.updateById(c);
            }
            case CmsReport.BIZ_TYPE_KB -> {
                KbEntry e = kbEntryMapper.selectById(bizId);
                if (e == null) {
                    throw new BizException(ResultCode.BAD_REQUEST, "目标科普内容不存在");
                }
                e.setStatus(status);
                kbEntryMapper.updateById(e);
            }
            default -> throw new BizException(ResultCode.BAD_REQUEST, "不支持的业务类型");
        }
    }

    private Long resolveContentAuthorId(Integer bizType, Long bizId) {
        return switch (bizType) {
            case CmsReport.BIZ_TYPE_QUESTION -> {
                QaQuestion q = qaQuestionMapper.selectById(bizId);
                if (q == null) {
                    throw new BizException(ResultCode.BAD_REQUEST, "目标问题不存在");
                }
                yield q.getUserId();
            }
            case CmsReport.BIZ_TYPE_ANSWER -> {
                QaAnswer a = qaAnswerMapper.selectById(bizId);
                if (a == null) {
                    throw new BizException(ResultCode.BAD_REQUEST, "目标回答不存在");
                }
                yield a.getUserId();
            }
            case CmsReport.BIZ_TYPE_COMMENT -> {
                QaComment c = qaCommentMapper.selectById(bizId);
                if (c == null) {
                    throw new BizException(ResultCode.BAD_REQUEST, "目标评论不存在");
                }
                yield c.getUserId();
            }
            case CmsReport.BIZ_TYPE_KB -> {
                KbEntry e = kbEntryMapper.selectById(bizId);
                if (e == null) {
                    throw new BizException(ResultCode.BAD_REQUEST, "目标科普内容不存在");
                }
                yield e.getAuthorUserId();
            }
            default -> throw new BizException(ResultCode.BAD_REQUEST, "不支持的业务类型");
        };
    }

    private String resolveContentTitle(Integer bizType, Long bizId) {
        return switch (bizType) {
            case CmsReport.BIZ_TYPE_QUESTION -> {
                QaQuestion q = qaQuestionMapper.selectById(bizId);
                yield q == null ? "问题#" + bizId : q.getTitle();
            }
            case CmsReport.BIZ_TYPE_ANSWER -> {
                QaAnswer a = qaAnswerMapper.selectById(bizId);
                if (a == null) {
                    yield "回答#" + bizId;
                }
                QaQuestion q = qaQuestionMapper.selectById(a.getQuestionId());
                yield q == null ? "回答#" + bizId : q.getTitle();
            }
            case CmsReport.BIZ_TYPE_COMMENT -> {
                QaComment c = qaCommentMapper.selectById(bizId);
                if (c == null) {
                    yield "评论#" + bizId;
                }
                QaAnswer a = qaAnswerMapper.selectById(c.getBizId());
                if (a == null) {
                    yield "评论#" + bizId;
                }
                QaQuestion q = qaQuestionMapper.selectById(a.getQuestionId());
                yield q == null ? "评论#" + bizId : q.getTitle();
            }
            case CmsReport.BIZ_TYPE_KB -> {
                KbEntry e = kbEntryMapper.selectById(bizId);
                yield e == null ? "科普#" + bizId : e.getTitle();
            }
            default -> "内容#" + bizId;
        };
    }

    private void createReportOutcomeNotifyToAuthor(Long receiverId, CmsReport report, String contentTitle, String resultText) {
        if (receiverId == null) {
            return;
        }
        NotifyMessage notify = new NotifyMessage();
        notify.setReceiverId(receiverId);
        notify.setType(NOTIFY_TYPE_REPORT_FEEDBACK);
        notify.setBizType(report.getBizType());
        notify.setBizId(report.getId());
        notify.setTitle("举报处理通知");
        notify.setContent(buildOutcomeContent(contentTitle, resultText, report.getHandleResult()));
        notify.setIsRead(0);
        notifyMessageMapper.insert(notify);
    }

    private void createReportOutcomeNotifyToReporter(Long receiverId, CmsReport report, String contentTitle, String resultText) {
        if (receiverId == null) {
            return;
        }
        NotifyMessage notify = new NotifyMessage();
        notify.setReceiverId(receiverId);
        notify.setType(NOTIFY_TYPE_REPORT_FEEDBACK);
        notify.setBizType(report.getBizType());
        notify.setBizId(report.getId());
        notify.setTitle("举报反馈通知");
        notify.setContent(buildOutcomeContent(contentTitle, resultText, report.getHandleResult()));
        notify.setIsRead(0);
        notifyMessageMapper.insert(notify);
    }

    private String buildOutcomeContent(String contentTitle, String resultText, String handleResult) {
        String titleSummary = truncateForList(StringUtils.hasText(contentTitle) ? contentTitle.trim() : "-");
        String resultSource = StringUtils.hasText(handleResult) ? handleResult.trim() : resultText;
        String resultSummary = truncateForList(StringUtils.hasText(resultSource) ? resultSource : "-");
        return "内容：" + titleSummary + "；处理结果：" + resultSummary;
    }

    private String truncateForList(String input) {
        if (!StringUtils.hasText(input)) {
            return "";
        }
        String s = input.trim();
        return s.length() > 10 ? s.substring(0, 10) + "..." : s;
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof SecurityUser su)) {
            return null;
        }
        return su.getId();
    }
}
