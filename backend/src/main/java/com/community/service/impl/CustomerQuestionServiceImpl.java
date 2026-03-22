package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.common.SecurityUser;
import com.community.dto.AppAnswerCreateDTO;
import com.community.dto.AppAnswerCommentCreateDTO;
import com.community.dto.AppAnswerUpdateDTO;
import com.community.dto.AppPageQueryDTO;
import com.community.dto.AppQuestionCreateDTO;
import com.community.dto.AppQuestionPageQueryDTO;
import com.community.dto.AppQuestionReportCreateDTO;
import com.community.dto.AppQuestionUpdateDTO;
import com.community.entity.CmsReport;
import com.community.entity.CmsSensitiveWord;
import com.community.entity.CmsAudit;
import com.community.entity.NotifyMessage;
import com.community.entity.QaAnswer;
import com.community.entity.QaCategory;
import com.community.entity.QaComment;
import com.community.entity.QaFavorite;
import com.community.entity.QaQuestion;
import com.community.entity.QaQuestionTag;
import com.community.entity.QaTag;
import com.community.entity.QaTopic;
import com.community.entity.QaVote;
import com.community.entity.User;
import com.community.entity.UserStat;
import com.community.entity.UserBrowseHistory;
import com.community.mapper.NotifyMessageMapper;
import com.community.mapper.QaAnswerMapper;
import com.community.mapper.QaCategoryMapper;
import com.community.mapper.CmsSensitiveWordMapper;
import com.community.mapper.CmsAuditMapper;
import com.community.mapper.CmsReportMapper;
import com.community.mapper.QaCommentMapper;
import com.community.mapper.QaFavoriteMapper;
import com.community.mapper.QaQuestionMapper;
import com.community.mapper.QaQuestionTagMapper;
import com.community.mapper.QaTagMapper;
import com.community.mapper.QaTopicMapper;
import com.community.mapper.QaVoteMapper;
import com.community.mapper.UserMapper;
import com.community.mapper.UserBrowseHistoryMapper;
import com.community.mapper.UserStatMapper;
import com.community.service.EsSearchService;
import com.community.service.CustomerQuestionService;
import com.community.service.RecommendationBehaviorService;
import com.community.vo.AppMyQuestionItemVO;
import com.community.vo.AppAnswerCommentVO;
import com.community.vo.AppAnswerDetailVO;
import com.community.vo.AppQuestionAnswerVO;
import com.community.vo.AppQuestionDetailVO;
import com.community.vo.AppQuestionListItemVO;
import com.community.vo.SearchQuestionDoc;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomerQuestionServiceImpl implements CustomerQuestionService {
    private final QaQuestionMapper qaQuestionMapper;
    private final QaAnswerMapper qaAnswerMapper;
    private final QaQuestionTagMapper qaQuestionTagMapper;
    private final QaTagMapper qaTagMapper;
    private final QaCategoryMapper qaCategoryMapper;
    private final QaCommentMapper qaCommentMapper;
    private final QaTopicMapper qaTopicMapper;
    private final QaVoteMapper qaVoteMapper;
    private final QaFavoriteMapper qaFavoriteMapper;
    private final NotifyMessageMapper notifyMessageMapper;
    private final CmsSensitiveWordMapper sensitiveWordMapper;
    private final CmsAuditMapper cmsAuditMapper;
    private final CmsReportMapper cmsReportMapper;
    private final UserMapper userMapper;
    private final UserBrowseHistoryMapper userBrowseHistoryMapper;
    private final UserStatMapper userStatMapper;
    private final ObjectMapper objectMapper;
    private final EsSearchService esSearchService;
    private final RecommendationBehaviorService recommendationBehaviorService;

    private static final int QUESTION_BROWSE_BIZ_TYPE = 1;
    private static final int SENSITIVE_LEVEL_REVIEW = 1;
    private static final int AUDIT_TRIGGER_RULE = 1;
    private static final int AUDIT_TYPE_RULE = 1;

    @Value("${qa.view-dedup-minutes:5}")
    private long viewDedupMinutes;

    @Override
    public PageInfo<AppQuestionListItemVO> page(AppQuestionPageQueryDTO query) {
        int page = query == null || query.getPage() == null || query.getPage() <= 0 ? 1 : query.getPage();
        int pageSize = query == null || query.getPageSize() == null || query.getPageSize() <= 0 ? 10
                : query.getPageSize();
        PageHelper.startPage(page, Math.min(pageSize, 50));
        List<AppQuestionListItemVO> rows = qaQuestionMapper.selectAppQuestionPage(
                query == null ? null : query.getKeyword(),
                query == null ? null : query.getCategoryId(),
                query == null ? null : query.getTopicId(),
                query == null ? null : query.getSortBy(),
                query == null ? null : query.getOnlyUnsolved(),
                requireUserId(),
                false);
        rows.forEach(this::fillImageUrls);
        return new PageInfo<>(rows);
    }

    @Override
    public PageInfo<AppMyQuestionItemVO> myQuestions(AppPageQueryDTO query) {
        Long userId = requireUserId();
        int page = query == null || query.getPage() == null || query.getPage() <= 0 ? 1 : query.getPage();
        int pageSize = query == null || query.getPageSize() == null || query.getPageSize() <= 0 ? 10
                : query.getPageSize();
        PageHelper.startPage(page, Math.min(pageSize, 50));
        List<AppMyQuestionItemVO> rows = qaQuestionMapper.selectMyEffectiveQuestions(userId);
        for (AppMyQuestionItemVO row : rows) {
            fillImageUrls(row);
            List<String> tags = qaQuestionTagMapper.selectTagNamesByQuestionId(row.getId());
            row.setTags(tags == null ? Collections.emptyList() : tags);
        }
        return new PageInfo<>(rows);
    }

    @Override
    public AppQuestionDetailVO detail(Long id) {
        Long userId = requireUserId();
        AppQuestionDetailVO vo = qaQuestionMapper.selectAppQuestionDetail(id);
        if (vo == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "question not found");
        }

        QaQuestion question = qaQuestionMapper.selectById(id);
        if (question == null || question.getDeleteFlag() == null || question.getDeleteFlag() != 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "question not found");
        }
        if ((question.getStatus() == null || question.getStatus() != 1) && !userId.equals(question.getUserId())) {
            throw new BizException(ResultCode.BAD_REQUEST, "question is not visible");
        }

        fillImageUrls(vo);

        boolean isAuthor = userId.equals(question.getUserId());
        if (!isAuthor) {
            boolean counted = recordQuestionBrowseIfNeeded(userId, id);
            if (counted) {
                qaQuestionMapper.incrementViewCount(id);
                vo.setViewCount((vo.getViewCount() == null ? 0 : vo.getViewCount()) + 1);
                recommendationBehaviorService.recordQuestionView(userId, id, question.getCategoryId());
            }
        }

        List<String> tags = qaQuestionTagMapper.selectTagNamesByQuestionId(id);
        vo.setTags(tags == null ? Collections.emptyList() : tags);
        vo.setLiked(isQuestionLikedByUser(id, userId));
        vo.setFavorited(isQuestionFavoritedByUser(id, userId));

        List<AppQuestionAnswerVO> answers = qaAnswerMapper.selectAppQuestionAnswers(id);
        for (AppQuestionAnswerVO answer : answers) {
            fillImageUrls(answer);
            answer.setCanEdit(userId.equals(answer.getAuthorId()));
            answer.setCanDelete(userId.equals(answer.getAuthorId()) || userId.equals(question.getUserId()));
            answer.setCommentCount(countAnswerComments(answer.getId()));
            answer.setFavoriteCount(countAnswerFavorites(answer.getId()));
            answer.setLiked(isAnswerLikedByUser(answer.getId(), userId));
            answer.setFavorited(isAnswerFavoritedByUser(answer.getId(), userId));
            boolean isBest = question.getAcceptedAnswerId() != null
                    && question.getAcceptedAnswerId().equals(answer.getId());
            answer.setBestAnswer(isBest);
            answer.setCanRecommend(userId.equals(question.getUserId()));
        }
        answers.sort(
                Comparator.comparing((AppQuestionAnswerVO a) -> Boolean.TRUE.equals(a.getBestAnswer()) ? 0 : 1)
                        .thenComparing(AppQuestionAnswerVO::getCreatedAt,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(AppQuestionAnswerVO::getId, Comparator.nullsLast(Comparator.naturalOrder())));
        vo.setAnswers(answers);
        return vo;
    }

    @Override
    @Transactional
    public Long createQuestion(AppQuestionCreateDTO dto) {
        Long userId = requireUserId();
        assertUserCanPublish(userId);
        validateQuestionRef(dto.getCategoryId(), dto.getTopicId());
        SensitiveScanResult sensitive = scanSensitiveWords(dto.getTitle(), dto.getContent());
        throwIfBlocked("Question", sensitive);

        QaQuestion question = new QaQuestion();
        question.setUserId(userId);
        question.setCategoryId(dto.getCategoryId());
        question.setTopicId(dto.getTopicId());
        question.setTitle(dto.getTitle().trim());
        question.setContent(dto.getContent());
        question.setImageUrls(serializeImageUrls(dto.getImageUrls()));
        question.setStatus(1);
        question.setRejectReason(null);
        question.setViewCount(0);
        question.setAnswerCount(0);
        question.setLikeCount(0);
        question.setFavoriteCount(0);
        question.setDeleteFlag(0);
        question.setLastActiveAt(LocalDateTime.now());
        qaQuestionMapper.insert(question);
        indexQuestionForEs(question);
        createOrRefreshRuleAudit(1, question.getId(), userId, sensitive.reviewHits());

        replaceQuestionTags(question.getId(), resolveQuestionTagIds(dto.getTagIds(), dto.getTagNames()));
        increaseTopicQuestionCount(dto.getTopicId(), 1);
        adjustUserQuestionCount(userId, 1);
        return question.getId();
    }

    @Override
    @Transactional
    public void updateQuestion(Long id, AppQuestionUpdateDTO dto) {
        Long userId = requireUserId();
        QaQuestion question = qaQuestionMapper.selectById(id);
        if (question == null || question.getDeleteFlag() == null || question.getDeleteFlag() != 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "question not found");
        }
        if (!userId.equals(question.getUserId())) {
            throw new BizException(ResultCode.FORBIDDEN, "no permission to update this question");
        }
        validateQuestionRef(dto.getCategoryId(), dto.getTopicId());
        SensitiveScanResult sensitive = scanSensitiveWords(dto.getTitle(), dto.getContent());
        throwIfBlocked("Question", sensitive);

        Long oldTopicId = question.getTopicId();
        question.setTitle(dto.getTitle().trim());
        question.setContent(dto.getContent());
        question.setImageUrls(serializeImageUrls(dto.getImageUrls()));
        question.setCategoryId(dto.getCategoryId());
        question.setTopicId(dto.getTopicId());
        question.setLastActiveAt(LocalDateTime.now());
        qaQuestionMapper.updateById(question);
        indexQuestionForEs(question);
        createOrRefreshRuleAudit(1, question.getId(), userId, sensitive.reviewHits());

        replaceQuestionTags(id, dto.getTagIds());
        if (oldTopicId == null ? dto.getTopicId() != null : !oldTopicId.equals(dto.getTopicId())) {
            increaseTopicQuestionCount(oldTopicId, -1);
            increaseTopicQuestionCount(dto.getTopicId(), 1);
        }
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        Long userId = requireUserId();
        QaQuestion question = qaQuestionMapper.selectById(id);
        if (question == null || question.getDeleteFlag() == null || question.getDeleteFlag() != 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "question not found");
        }
        if (!userId.equals(question.getUserId())) {
            throw new BizException(ResultCode.FORBIDDEN, "no permission to delete this question");
        }

        // delete question likes and related notifications
        qaVoteMapper.delete(new LambdaQueryWrapper<QaVote>()
                .eq(QaVote::getBizId, id)
                .eq(QaVote::getVoteType, 1));
        notifyMessageMapper.delete(new LambdaQueryWrapper<NotifyMessage>()
                .eq(NotifyMessage::getType, 2)
                .eq(NotifyMessage::getBizType, 1)
                .eq(NotifyMessage::getBizId, id));
        removeQuestionTags(id);

        qaQuestionMapper.update(null, new LambdaUpdateWrapper<QaQuestion>()
                .eq(QaQuestion::getId, id)
                .set(QaQuestion::getStatus, QaQuestion.STATUS_DELETED_BY_USER)
                .set(QaQuestion::getDeleteFlag, 1)
                .set(QaQuestion::getUpdatedAt, LocalDateTime.now()));
        if (question.getStatus() != null && question.getStatus() == QaQuestion.STATUS_PUBLISHED) {
            increaseTopicQuestionCount(question.getTopicId(), -1);
        }
        adjustUserQuestionCount(userId, -1);
        esSearchService.syncQuestionById(id);
    }

    @Override
    @Transactional
    public void setQuestionSelfOnly(Long id) {
        Long userId = requireUserId();
        QaQuestion question = qaQuestionMapper.selectById(id);
        if (question == null || question.getDeleteFlag() == null || question.getDeleteFlag() != 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "question not found");
        }
        if (!userId.equals(question.getUserId())) {
            throw new BizException(ResultCode.FORBIDDEN, "no permission to update visibility");
        }
        if (question.getStatus() != null && question.getStatus() == QaQuestion.STATUS_DELETED_BY_USER) {
            throw new BizException(ResultCode.BAD_REQUEST, "question already deleted");
        }
        if (question.getStatus() != null && question.getStatus() == QaQuestion.STATUS_SELF_ONLY) {
            return;
        }
        qaQuestionMapper.update(null, new LambdaUpdateWrapper<QaQuestion>()
                .eq(QaQuestion::getId, id)
                .set(QaQuestion::getStatus, QaQuestion.STATUS_SELF_ONLY)
                .set(QaQuestion::getUpdatedAt, LocalDateTime.now()));
        if (question.getStatus() != null && question.getStatus() == QaQuestion.STATUS_PUBLISHED) {
            increaseTopicQuestionCount(question.getTopicId(), -1);
        }
        esSearchService.syncQuestionById(id);
    }

    @Override
    @Transactional
    public void setQuestionPublic(Long id) {
        Long userId = requireUserId();
        QaQuestion question = qaQuestionMapper.selectById(id);
        if (question == null || question.getDeleteFlag() == null || question.getDeleteFlag() != 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "question not found");
        }
        if (!userId.equals(question.getUserId())) {
            throw new BizException(ResultCode.FORBIDDEN, "no permission to update visibility");
        }
        if (question.getStatus() != null && question.getStatus() == QaQuestion.STATUS_DELETED_BY_USER) {
            throw new BizException(ResultCode.BAD_REQUEST, "question already deleted");
        }
        if (question.getStatus() != null && question.getStatus() == QaQuestion.STATUS_PUBLISHED) {
            return;
        }
        qaQuestionMapper.update(null, new LambdaUpdateWrapper<QaQuestion>()
                .eq(QaQuestion::getId, id)
                .set(QaQuestion::getStatus, QaQuestion.STATUS_PUBLISHED)
                .set(QaQuestion::getUpdatedAt, LocalDateTime.now()));
        if (question.getStatus() != null && question.getStatus() == QaQuestion.STATUS_SELF_ONLY) {
            increaseTopicQuestionCount(question.getTopicId(), 1);
        }
        esSearchService.syncQuestionById(id);
    }

    @Override
    @Transactional
    public Long reportQuestion(Long id, AppQuestionReportCreateDTO dto) {
        Long userId = requireUserId();
        QaQuestion question = qaQuestionMapper.selectById(id);
        if (question == null || question.getDeleteFlag() == null || question.getDeleteFlag() != 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "找不到问题");
        }
        if (userId.equals(question.getUserId())) {
            throw new BizException(ResultCode.BAD_REQUEST, "不能举报你自己发布的问题");
        }
        if (question.getStatus() == null || question.getStatus() != QaQuestion.STATUS_PUBLISHED) {
            throw new BizException(ResultCode.BAD_REQUEST, "这个问题不属于可举报内容");
        }
        assertNoPendingDuplicateReport(userId, CmsReport.BIZ_TYPE_QUESTION, id);
        validateNoSensitiveWords("Report", dto.getReasonDetail());

        CmsReport report = new CmsReport();
        report.setBizType(1);
        report.setBizId(id);
        report.setReasonType(resolveReasonType(dto.getReasonCode()));
        report.setReporterId(userId);
        report.setReasonCode(dto.getReasonCode().trim());
        report.setReasonDetail(StringUtils.hasText(dto.getReasonDetail()) ? dto.getReasonDetail().trim() : null);
        report.setStatus(1);
        cmsReportMapper.insert(report);
        return report.getId();
    }

    @Override
    @Transactional
    public Long reportAnswer(Long id, AppQuestionReportCreateDTO dto) {
        Long userId = requireUserId();
        QaAnswer answer = qaAnswerMapper.selectById(id);
        if (answer == null || answer.getDeleteFlag() == null || answer.getDeleteFlag() != 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "找不到答案");
        }
        if (userId.equals(answer.getUserId())) {
            throw new BizException(ResultCode.BAD_REQUEST, "不能举报你自己发布的回答");
        }
        if (answer.getStatus() == null || answer.getStatus() != 1) {
            throw new BizException(ResultCode.BAD_REQUEST, "这个答案不属于可举报内容");
        }
        assertNoPendingDuplicateReport(userId, CmsReport.BIZ_TYPE_ANSWER, id);
        validateNoSensitiveWords("Report", dto.getReasonDetail());

        CmsReport report = new CmsReport();
        report.setBizType(2);
        report.setBizId(id);
        report.setReasonType(resolveReasonType(dto.getReasonCode()));
        report.setReporterId(userId);
        report.setReasonCode(dto.getReasonCode().trim());
        report.setReasonDetail(StringUtils.hasText(dto.getReasonDetail()) ? dto.getReasonDetail().trim() : null);
        report.setStatus(1);
        cmsReportMapper.insert(report);
        return report.getId();
    }

    @Override
    @Transactional
    public Long createAnswer(Long questionId, AppAnswerCreateDTO dto) {
        Long userId = requireUserId();
        assertUserCanPublish(userId);
        QaQuestion question = qaQuestionMapper.selectById(questionId);
        if (question == null || question.getDeleteFlag() == null || question.getDeleteFlag() != 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "question not found");
        }
        if (question.getStatus() == null || question.getStatus() != 1) {
            throw new BizException(ResultCode.BAD_REQUEST, "question cannot be answered");
        }
        SensitiveScanResult sensitive = scanSensitiveWords(dto.getContent());
        throwIfBlocked("Answer", sensitive);

        QaAnswer answer = new QaAnswer();
        answer.setQuestionId(questionId);
        answer.setUserId(userId);
        answer.setContent(dto.getContent().trim());
        answer.setImageUrls(serializeImageUrls(dto.getImageUrls()));
        answer.setStatus(1);
        answer.setDeleteFlag(0);
        answer.setLikeCount(0);
        answer.setIsAnonymous(dto.getIsAnonymous() != null && dto.getIsAnonymous() == 1 ? 1 : 0);
        qaAnswerMapper.insert(answer);
        createOrRefreshRuleAudit(2, answer.getId(), userId, sensitive.reviewHits());

        qaQuestionMapper.updateAnswerCount(questionId, 1);
        qaQuestionMapper.update(null, new LambdaUpdateWrapper<QaQuestion>()
                .eq(QaQuestion::getId, questionId)
                .set(QaQuestion::getLastActiveAt, LocalDateTime.now()));
        QaQuestion updatedQuestion = qaQuestionMapper.selectById(questionId);
        if (updatedQuestion != null) {
            indexQuestionForEs(updatedQuestion);
        }
        adjustUserAnswerCount(userId, 1);

        createNotifyIfNeeded(
                question.getUserId(),
                6,
                2,
                answer.getId(),
                "新回答",
                actorName(userId) + " 回答了你的问题：" + shorten(question.getTitle(), 26));
        return answer.getId();
    }

    @Override
    @Transactional
    public void updateAnswer(Long answerId, AppAnswerUpdateDTO dto) {
        Long userId = requireUserId();
        QaAnswer answer = qaAnswerMapper.selectById(answerId);
        if (answer == null || answer.getDeleteFlag() == null || answer.getDeleteFlag() != 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "answer not found");
        }
        if (!userId.equals(answer.getUserId())) {
            throw new BizException(ResultCode.FORBIDDEN, "no permission to update this answer");
        }
        SensitiveScanResult sensitive = scanSensitiveWords(dto.getContent());
        throwIfBlocked("Answer", sensitive);
        answer.setContent(dto.getContent().trim());
        answer.setImageUrls(serializeImageUrls(dto.getImageUrls()));
        qaAnswerMapper.updateById(answer);
        createOrRefreshRuleAudit(2, answer.getId(), userId, sensitive.reviewHits());

        qaQuestionMapper.update(null, new LambdaUpdateWrapper<QaQuestion>()
                .eq(QaQuestion::getId, answer.getQuestionId())
                .set(QaQuestion::getLastActiveAt, LocalDateTime.now()));
    }

    @Override
    @Transactional
    public void deleteAnswer(Long answerId) {
        Long userId = requireUserId();
        QaAnswer answer = qaAnswerMapper.selectById(answerId);
        if (answer == null || answer.getDeleteFlag() == null || answer.getDeleteFlag() != 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "answer not found");
        }
        QaQuestion question = qaQuestionMapper.selectById(answer.getQuestionId());
        if (question == null || question.getDeleteFlag() == null || question.getDeleteFlag() != 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "question not found");
        }
        boolean canDelete = userId.equals(answer.getUserId()) || userId.equals(question.getUserId());
        if (!canDelete) {
            throw new BizException(ResultCode.FORBIDDEN, "no permission to delete this answer");
        }

        qaAnswerMapper.update(null, new LambdaUpdateWrapper<QaAnswer>()
                .eq(QaAnswer::getId, answerId)
                .set(QaAnswer::getDeleteFlag, 1)
                .set(QaAnswer::getStatus, 0)
                .set(QaAnswer::getUpdatedAt, LocalDateTime.now()));

        if (answer.getStatus() != null && answer.getStatus() == 1) {
            qaQuestionMapper.updateAnswerCount(answer.getQuestionId(), -1);
        }
        if (question.getAcceptedAnswerId() != null && question.getAcceptedAnswerId().equals(answerId)) {
            qaQuestionMapper.update(
                    null,
                    new LambdaUpdateWrapper<QaQuestion>()
                            .eq(QaQuestion::getId, question.getId())
                            .set(QaQuestion::getAcceptedAnswerId, null)
                            .set(QaQuestion::getAcceptedAt, null)
                            .set(QaQuestion::getLastActiveAt, LocalDateTime.now()));
        } else {
            qaQuestionMapper.update(null, new LambdaUpdateWrapper<QaQuestion>()
                    .eq(QaQuestion::getId, answer.getQuestionId())
                    .set(QaQuestion::getLastActiveAt, LocalDateTime.now()));
        }
        adjustUserAnswerCount(answer.getUserId(), -1);
        esSearchService.syncQuestionById(answer.getQuestionId());
        adjustUserAnswerLikeReceivedCount(answer.getUserId(),
                -(answer.getLikeCount() == null ? 0 : answer.getLikeCount()));
    }

    @Override
    @Transactional
    public AppQuestionDetailVO toggleQuestionLike(Long questionId) {
        Long userId = requireUserId();
        QaQuestion question = requirePublishedQuestion(questionId);

        QaVote existed = qaVoteMapper.selectOne(new LambdaQueryWrapper<QaVote>()
                .eq(QaVote::getBizType, 1)
                .eq(QaVote::getBizId, questionId)
                .eq(QaVote::getUserId, userId)
                .eq(QaVote::getVoteType, 1));

        if (existed == null) {
            QaVote vote = new QaVote();
            vote.setBizType(1);
            vote.setBizId(questionId);
            vote.setUserId(userId);
            vote.setVoteType(1);
            qaVoteMapper.insert(vote);
            question.setLikeCount((question.getLikeCount() == null ? 0 : question.getLikeCount()) + 1);
            createNotifyIfNeeded(
                    question.getUserId(),
                    2,
                    1,
                    questionId,
                    "收到点赞",
                    actorName(userId) + " 点赞了你的问题：" + shorten(question.getTitle(), 26));
            recommendationBehaviorService.recordQuestionLike(userId, questionId, question.getCategoryId(), true);
        } else {
            qaVoteMapper.deleteById(existed.getId());
            int old = question.getLikeCount() == null ? 0 : question.getLikeCount();
            question.setLikeCount(Math.max(0, old - 1));
            recommendationBehaviorService.recordQuestionLike(userId, questionId, question.getCategoryId(), false);
        }
        qaQuestionMapper.updateById(question);
        return detail(questionId);
    }

    @Override
    @Transactional
    public AppQuestionDetailVO toggleQuestionFavorite(Long questionId) {
        Long userId = requireUserId();
        QaQuestion question = requirePublishedQuestion(questionId);

        QaFavorite existed = qaFavoriteMapper.selectOne(new LambdaQueryWrapper<QaFavorite>()
                .eq(QaFavorite::getQuestionId, questionId)
                .eq(QaFavorite::getUserId, userId));

        if (existed == null) {
            QaFavorite favorite = new QaFavorite();
            favorite.setQuestionId(questionId);
            favorite.setUserId(userId);
            qaFavoriteMapper.insert(favorite);
            question.setFavoriteCount((question.getFavoriteCount() == null ? 0 : question.getFavoriteCount()) + 1);
            createNotifyIfNeeded(
                    question.getUserId(),
                    3,
                    1,
                    questionId,
                    "收到收藏",
                    actorName(userId) + " 收藏了你的问题：" + shorten(question.getTitle(), 26));
            recommendationBehaviorService.recordQuestionFavorite(userId, questionId, question.getCategoryId(), true);
        } else {
            qaFavoriteMapper.deleteById(existed.getId());
            int old = question.getFavoriteCount() == null ? 0 : question.getFavoriteCount();
            question.setFavoriteCount(Math.max(0, old - 1));
            recommendationBehaviorService.recordQuestionFavorite(userId, questionId, question.getCategoryId(), false);
        }
        qaQuestionMapper.updateById(question);
        return detail(questionId);
    }

    @Override
    public AppAnswerDetailVO answerDetail(Long answerId) {
        Long userId = requireUserId();
        AppQuestionAnswerVO answer = qaAnswerMapper.selectAppAnswerById(answerId);
        if (answer == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "answer not found");
        }

        QaQuestion question = qaQuestionMapper.selectById(answer.getQuestionId());
        if (question == null || question.getDeleteFlag() == null || question.getDeleteFlag() != 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "question not found");
        }
        if ((question.getStatus() == null || question.getStatus() != 1) && !userId.equals(question.getUserId())) {
            throw new BizException(ResultCode.BAD_REQUEST, "question is not visible");
        }

        fillImageUrls(answer);
        answer.setCanEdit(userId.equals(answer.getAuthorId()));
        answer.setCanDelete(userId.equals(answer.getAuthorId()) || userId.equals(question.getUserId()));
        answer.setCommentCount(countAnswerComments(answer.getId()));
        answer.setFavoriteCount(countAnswerFavorites(answer.getId()));
        answer.setLiked(isAnswerLikedByUser(answer.getId(), userId));
        answer.setFavorited(isAnswerFavoritedByUser(answer.getId(), userId));
        boolean isBest = question.getAcceptedAnswerId() != null
                && question.getAcceptedAnswerId().equals(answer.getId());
        answer.setBestAnswer(isBest);
        answer.setCanRecommend(userId.equals(question.getUserId()));

        AppAnswerDetailVO vo = new AppAnswerDetailVO();
        vo.setAnswer(answer);
        vo.setQuestionId(question.getId());
        vo.setQuestionTitle(question.getTitle());
        vo.setComments(answerComments(answerId));
        return vo;
    }

    @Override
    @Transactional
    public AppAnswerDetailVO toggleAnswerLike(Long answerId) {
        Long userId = requireUserId();
        QaAnswer answer = requirePublishedAnswer(answerId);

        QaVote existed = qaVoteMapper.selectOne(new LambdaQueryWrapper<QaVote>()
                .eq(QaVote::getBizType, 2)
                .eq(QaVote::getBizId, answerId)
                .eq(QaVote::getUserId, userId)
                .eq(QaVote::getVoteType, 1));
        if (existed == null) {
            QaVote vote = new QaVote();
            vote.setBizType(2);
            vote.setBizId(answerId);
            vote.setUserId(userId);
            vote.setVoteType(1);
            boolean inserted = insertAnswerVote(vote);
            if (inserted) {
                answer.setLikeCount((answer.getLikeCount() == null ? 0 : answer.getLikeCount()) + 1);
                adjustUserAnswerLikeReceivedCount(answer.getUserId(), 1);
                createNotifyIfNeeded(
                        answer.getUserId(),
                        2,
                        2,
                        answerId,
                        "收到点赞",
                        actorName(userId) + " 点赞了你的回答");
                recommendationBehaviorService.recordAnswerLike(userId, answerId,
                        resolveQuestionCategoryId(answer.getQuestionId()), true);
            }
        } else {
            qaVoteMapper.deleteById(existed.getId());
            int old = answer.getLikeCount() == null ? 0 : answer.getLikeCount();
            answer.setLikeCount(Math.max(0, old - 1));
            adjustUserAnswerLikeReceivedCount(answer.getUserId(), -1);
            recommendationBehaviorService.recordAnswerLike(userId, answerId,
                    resolveQuestionCategoryId(answer.getQuestionId()), false);
        }
        qaAnswerMapper.updateById(answer);
        return answerDetail(answerId);
    }

    @Override
    @Transactional
    public AppAnswerDetailVO toggleAnswerFavorite(Long answerId) {
        Long userId = requireUserId();
        QaAnswer answer = requirePublishedAnswer(answerId);

        QaVote existed = qaVoteMapper.selectOne(new LambdaQueryWrapper<QaVote>()
                .eq(QaVote::getBizType, 2)
                .eq(QaVote::getBizId, answerId)
                .eq(QaVote::getUserId, userId)
                .eq(QaVote::getVoteType, 2));
        if (existed == null) {
            QaVote vote = new QaVote();
            vote.setBizType(2);
            vote.setBizId(answerId);
            vote.setUserId(userId);
            vote.setVoteType(2);
            boolean inserted = insertAnswerVote(vote);
            if (inserted) {
                createNotifyIfNeeded(
                        answer.getUserId(),
                        3,
                        2,
                        answerId,
                        "收到收藏",
                        actorName(userId) + " 收藏了你的回答");
                recommendationBehaviorService.recordAnswerFavorite(userId, answerId,
                        resolveQuestionCategoryId(answer.getQuestionId()), true);
            }
        } else {
            qaVoteMapper.deleteById(existed.getId());
            recommendationBehaviorService.recordAnswerFavorite(userId, answerId,
                    resolveQuestionCategoryId(answer.getQuestionId()), false);
        }
        return answerDetail(answerId);
    }

    @Override
    @Transactional
    public void recommendBestAnswer(Long questionId, Long answerId) {
        Long userId = requireUserId();
        QaQuestion question = qaQuestionMapper.selectById(questionId);
        if (question == null || question.getDeleteFlag() == null || question.getDeleteFlag() != 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "question not found");
        }
        if (!userId.equals(question.getUserId())) {
            throw new BizException(ResultCode.FORBIDDEN, "only question author can recommend best answer");
        }

        QaAnswer answer = qaAnswerMapper.selectById(answerId);
        if (answer == null || answer.getDeleteFlag() == null || answer.getDeleteFlag() != 0
                || answer.getStatus() == null || answer.getStatus() != 1) {
            throw new BizException(ResultCode.BAD_REQUEST, "answer not found");
        }
        if (!questionId.equals(answer.getQuestionId())) {
            throw new BizException(ResultCode.BAD_REQUEST, "answer does not belong to this question");
        }

        boolean cancelBest = question.getAcceptedAnswerId() != null && question.getAcceptedAnswerId().equals(answerId);
        if (cancelBest) {
            qaQuestionMapper.update(
                    null,
                    new LambdaUpdateWrapper<QaQuestion>()
                            .eq(QaQuestion::getId, questionId)
                            .set(QaQuestion::getAcceptedAnswerId, null)
                            .set(QaQuestion::getAcceptedAt, null));
            createNotifyIfNeeded(
                    answer.getUserId(),
                    1,
                    2,
                    answerId,
                    "最佳回答变更",
                    "你的回答已取消最佳");
        } else {
            question.setAcceptedAnswerId(answerId);
            question.setAcceptedAt(LocalDateTime.now());
            createNotifyIfNeeded(
                    answer.getUserId(),
                    1,
                    2,
                    answerId,
                    "最佳回答",
                    "你的回答被采纳为最佳");
            qaQuestionMapper.update(
                    null,
                    new LambdaUpdateWrapper<QaQuestion>()
                            .eq(QaQuestion::getId, questionId)
                            .set(QaQuestion::getAcceptedAnswerId, answerId)
                            .set(QaQuestion::getAcceptedAt, LocalDateTime.now()));
        }
    }

    @Override
    @Transactional
    public void cancelBestAnswer(Long questionId) {
        Long userId = requireUserId();
        QaQuestion question = qaQuestionMapper.selectById(questionId);
        if (question == null || question.getDeleteFlag() == null || question.getDeleteFlag() != 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "question not found");
        }
        if (!userId.equals(question.getUserId())) {
            throw new BizException(ResultCode.FORBIDDEN, "only question author can cancel best answer");
        }
        if (question.getAcceptedAnswerId() == null) {
            return;
        }

        QaAnswer accepted = qaAnswerMapper.selectById(question.getAcceptedAnswerId());
        if (accepted != null && accepted.getUserId() != null) {
            createNotifyIfNeeded(
                    accepted.getUserId(),
                    1,
                    2,
                    accepted.getId(),
                    "最佳回答变更",
                    "你的回答已取消最佳");
        }
        qaQuestionMapper.update(
                null,
                new LambdaUpdateWrapper<QaQuestion>()
                        .eq(QaQuestion::getId, questionId)
                        .set(QaQuestion::getAcceptedAnswerId, null)
                        .set(QaQuestion::getAcceptedAt, null));
    }

    @Override
    public List<AppAnswerCommentVO> answerComments(Long answerId) {
        requirePublishedAnswer(answerId);
        return qaCommentMapper.selectAnswerComments(answerId);
    }

    @Override
    @Transactional
    public Long createAnswerComment(Long answerId, AppAnswerCommentCreateDTO dto) {
        Long userId = requireUserId();
        assertUserCanPublish(userId);
        QaAnswer answer = requirePublishedAnswer(answerId);
        SensitiveScanResult sensitive = scanSensitiveWords(dto.getContent());
        throwIfBlocked("Comment", sensitive);

        QaComment comment = new QaComment();
        comment.setBizType(2);
        comment.setBizId(answerId);
        comment.setUserId(userId);
        Long parentId = dto.getParentId();
        QaComment parentComment = null;
        if (parentId != null) {
            parentComment = qaCommentMapper.selectById(parentId);
            if (parentComment == null
                    || parentComment.getDeleteFlag() == null || parentComment.getDeleteFlag() != 0
                    || parentComment.getStatus() == null || parentComment.getStatus() != 1
                    || parentComment.getBizType() == null || parentComment.getBizType() != 2
                    || !answerId.equals(parentComment.getBizId())) {
                throw new BizException(ResultCode.BAD_REQUEST, "parent comment not found");
            }
        }
        comment.setParentId(parentId);
        comment.setContent(dto.getContent().trim());
        comment.setStatus(1);
        comment.setRejectReason(null);
        comment.setDeleteFlag(0);
        qaCommentMapper.insert(comment);
        createOrRefreshRuleAudit(3, comment.getId(), userId, sensitive.reviewHits());
        if (parentComment == null) {
            // first-level comment: notify answer author
            createNotifyIfNeeded(
                    answer.getUserId(),
                    5,
                    2,
                    answerId,
                    "收到评论",
                    actorName(userId) + " 评论了你的回答");
        } else {
            // reply comment: notify target comment author
            createNotifyIfNeeded(
                    parentComment.getUserId(),
                    5,
                    2,
                    answerId,
                    "收到回复",
                    actorName(userId) + " 回复了你的评论");
        }
        return comment.getId();
    }

    private void validateQuestionRef(Long categoryId, Long topicId) {
        if (categoryId != null) {
            QaCategory category = qaCategoryMapper.selectById(categoryId);
            if (category == null || category.getDeleteFlag() == null || category.getDeleteFlag() != 0
                    || category.getStatus() == null || category.getStatus() != 1) {
                throw new BizException(ResultCode.BAD_REQUEST, "category is invalid");
            }
        }

        if (topicId != null) {
            QaTopic topic = qaTopicMapper.selectById(topicId);
            if (topic == null || topic.getStatus() == null || topic.getStatus() != 1) {
                throw new BizException(ResultCode.BAD_REQUEST, "topic is invalid");
            }
        }
    }

    private void replaceQuestionTags(Long questionId, List<Long> tagIds) {
        Set<Long> uniqueTagIds;
        if (tagIds == null || tagIds.isEmpty()) {
            uniqueTagIds = Collections.emptySet();
        } else {
            LinkedHashSet<Long> sanitized = new LinkedHashSet<>();
            for (Long tagId : tagIds) {
                if (tagId != null && tagId > 0) {
                    sanitized.add(tagId);
                }
            }
            uniqueTagIds = sanitized;
        }
        if (!uniqueTagIds.isEmpty()) {
            List<QaTag> tags = qaTagMapper.selectBatchIds(uniqueTagIds);
            if (tags.size() != uniqueTagIds.size()) {
                throw new BizException(ResultCode.BAD_REQUEST, "tagIds contain invalid tag");
            }
            for (QaTag tag : tags) {
                if (tag.getStatus() == null || tag.getStatus() != 1) {
                    throw new BizException(ResultCode.BAD_REQUEST, "tag is disabled");
                }
            }
        }

        Set<Long> oldTagIds = loadQuestionTagIds(questionId);
        Set<Long> addedTagIds = new LinkedHashSet<>(uniqueTagIds);
        addedTagIds.removeAll(oldTagIds);
        Set<Long> removedTagIds = new LinkedHashSet<>(oldTagIds);
        removedTagIds.removeAll(uniqueTagIds);

        qaQuestionTagMapper.delete(new LambdaQueryWrapper<QaQuestionTag>()
                .eq(QaQuestionTag::getQuestionId, questionId));
        for (Long tagId : uniqueTagIds) {
            QaQuestionTag rel = new QaQuestionTag();
            rel.setQuestionId(questionId);
            rel.setTagId(tagId);
            qaQuestionTagMapper.insert(rel);
        }

        for (Long tagId : addedTagIds) {
            adjustTagUseCount(tagId, 1);
        }
        for (Long tagId : removedTagIds) {
            adjustTagUseCount(tagId, -1);
        }
    }

    private void removeQuestionTags(Long questionId) {
        Set<Long> oldTagIds = loadQuestionTagIds(questionId);
        if (oldTagIds.isEmpty()) {
            return;
        }
        qaQuestionTagMapper.delete(new LambdaQueryWrapper<QaQuestionTag>()
                .eq(QaQuestionTag::getQuestionId, questionId));
        for (Long tagId : oldTagIds) {
            adjustTagUseCount(tagId, -1);
        }
    }

    private Set<Long> loadQuestionTagIds(Long questionId) {
        if (questionId == null) {
            return Collections.emptySet();
        }
        List<QaQuestionTag> rels = qaQuestionTagMapper.selectList(new LambdaQueryWrapper<QaQuestionTag>()
                .eq(QaQuestionTag::getQuestionId, questionId));
        if (rels == null || rels.isEmpty()) {
            return Collections.emptySet();
        }
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        for (QaQuestionTag rel : rels) {
            if (rel != null && rel.getTagId() != null) {
                ids.add(rel.getTagId());
            }
        }
        return ids;
    }

    private void adjustTagUseCount(Long tagId, int delta) {
        if (tagId == null || delta == 0) {
            return;
        }
        QaTag tag = qaTagMapper.selectById(tagId);
        if (tag == null) {
            return;
        }
        int current = tag.getUseCount() == null ? 0 : tag.getUseCount();
        int next = Math.max(0, current + delta);
        tag.setUseCount(next);
        qaTagMapper.updateById(tag);
    }

    private List<Long> resolveQuestionTagIds(List<Long> tagIds, List<String> tagNames) {
        LinkedHashSet<Long> result = new LinkedHashSet<>();
        if (tagIds != null) {
            for (Long id : tagIds) {
                if (id != null && id > 0) {
                    result.add(id);
                }
            }
        }

        if (tagNames != null) {
            LinkedHashSet<String> normalizedNames = new LinkedHashSet<>();
            for (String name : tagNames) {
                if (!StringUtils.hasText(name)) {
                    continue;
                }
                String normalized = name.trim();
                if (!normalized.isEmpty()) {
                    normalizedNames.add(normalized);
                }
                if (normalizedNames.size() >= 5) {
                    break;
                }
            }

            for (String name : normalizedNames) {
                validateNoSensitiveWords("Tag", name);
                QaTag tag = qaTagMapper.selectOne(new LambdaQueryWrapper<QaTag>().eq(QaTag::getName, name));
                if (tag == null) {
                    tag = new QaTag();
                    tag.setName(name);
                    tag.setStatus(1);
                    tag.setUseCount(0);
                    qaTagMapper.insert(tag);
                }
                if (tag.getId() != null) {
                    result.add(tag.getId());
                }
            }
        }

        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        return result.stream().limit(5).toList();
    }

    private void validateNoSensitiveWords(String bizName, String... texts) {
        List<String> candidates = new ArrayList<>();
        if (texts != null) {
            for (String text : texts) {
                if (StringUtils.hasText(text)) {
                    candidates.add(text);
                }
            }
        }
        if (candidates.isEmpty()) {
            return;
        }

        List<CmsSensitiveWord> words = sensitiveWordMapper.selectList(new LambdaQueryWrapper<CmsSensitiveWord>()
                .eq(CmsSensitiveWord::getEnabled, 1));
        if (words == null || words.isEmpty()) {
            return;
        }

        for (String rawText : candidates) {
            String text = rawText.trim();
            if (!StringUtils.hasText(text)) {
                continue;
            }
            String lowerText = text.toLowerCase(Locale.ROOT);
            for (CmsSensitiveWord item : words) {
                String word = item.getWord();
                if (!StringUtils.hasText(word)) {
                    continue;
                }
                String hit = word.trim();
                if (hit.isEmpty()) {
                    continue;
                }
                if (text.contains(hit) || lowerText.contains(hit.toLowerCase(Locale.ROOT))) {
                    throw new BizException(ResultCode.BAD_REQUEST, bizName + " contains sensitive word: " + hit);
                }
            }
        }
    }

    private void increaseTopicQuestionCount(Long topicId, int delta) {
        if (topicId == null || delta == 0) {
            return;
        }
        QaTopic topic = qaTopicMapper.selectById(topicId);
        if (topic == null) {
            return;
        }
        int oldCount = topic.getQuestionCount() == null ? 0 : topic.getQuestionCount();
        topic.setQuestionCount(Math.max(0, oldCount + delta));
        qaTopicMapper.updateById(topic);
    }

    private void adjustUserQuestionCount(Long userId, int delta) {
        if (userId == null || delta == 0) {
            return;
        }
        UserStat stat = userStatMapper.selectById(userId);
        if (stat == null) {
            UserStat created = new UserStat();
            created.setUserId(userId);
            created.setQuestionCount(Math.max(delta, 0));
            created.setAnswerCount(0);
            created.setLikeReceivedCount(0);
            created.setFollowerCount(0);
            created.setFollowingCount(0);
            created.setUpdatedAt(LocalDateTime.now());
            userStatMapper.insert(created);
            return;
        }
        int oldCount = stat.getQuestionCount() == null ? 0 : stat.getQuestionCount();
        stat.setQuestionCount(Math.max(0, oldCount + delta));
        stat.setUpdatedAt(LocalDateTime.now());
        userStatMapper.updateById(stat);
    }

    private void adjustUserAnswerCount(Long userId, int delta) {
        if (userId == null || delta == 0) {
            return;
        }
        UserStat stat = userStatMapper.selectById(userId);
        if (stat == null) {
            UserStat created = new UserStat();
            created.setUserId(userId);
            created.setQuestionCount(0);
            created.setAnswerCount(Math.max(delta, 0));
            created.setLikeReceivedCount(0);
            created.setFollowerCount(0);
            created.setFollowingCount(0);
            created.setUpdatedAt(LocalDateTime.now());
            userStatMapper.insert(created);
            return;
        }
        int oldCount = stat.getAnswerCount() == null ? 0 : stat.getAnswerCount();
        stat.setAnswerCount(Math.max(0, oldCount + delta));
        stat.setUpdatedAt(LocalDateTime.now());
        userStatMapper.updateById(stat);
    }

    private void adjustUserAnswerLikeReceivedCount(Long userId, int delta) {
        if (userId == null || delta == 0) {
            return;
        }
        UserStat stat = userStatMapper.selectById(userId);
        if (stat == null) {
            UserStat created = new UserStat();
            created.setUserId(userId);
            created.setQuestionCount(0);
            created.setAnswerCount(0);
            created.setLikeReceivedCount(Math.max(delta, 0));
            created.setFollowerCount(0);
            created.setFollowingCount(0);
            created.setUpdatedAt(LocalDateTime.now());
            userStatMapper.insert(created);
            return;
        }
        int oldCount = stat.getLikeReceivedCount() == null ? 0 : stat.getLikeReceivedCount();
        stat.setLikeReceivedCount(Math.max(0, oldCount + delta));
        stat.setUpdatedAt(LocalDateTime.now());
        userStatMapper.updateById(stat);
    }

    private void indexQuestionForEs(QaQuestion question) {
        if (esSearchService == null || !esSearchService.isEnabled() || question == null || question.getId() == null) {
            return;
        }
        esSearchService.syncQuestionById(question.getId());
    }

    private SensitiveScanResult scanSensitiveWords(String... texts) {
        List<String> candidates = new ArrayList<>();
        if (texts != null) {
            for (String text : texts) {
                if (StringUtils.hasText(text)) {
                    candidates.add(text);
                }
            }
        }
        if (candidates.isEmpty()) {
            return new SensitiveScanResult(List.of(), List.of());
        }

        List<CmsSensitiveWord> words = sensitiveWordMapper.selectList(new LambdaQueryWrapper<CmsSensitiveWord>()
                .eq(CmsSensitiveWord::getEnabled, 1));
        if (words == null || words.isEmpty()) {
            return new SensitiveScanResult(List.of(), List.of());
        }

        List<SensitiveHit> blockedHits = new ArrayList<>();
        List<SensitiveHit> reviewHits = new ArrayList<>();
        Set<String> dedup = new LinkedHashSet<>();
        for (String rawText : candidates) {
            String text = rawText.trim();
            if (!StringUtils.hasText(text)) {
                continue;
            }
            String lowerText = text.toLowerCase(Locale.ROOT);
            for (CmsSensitiveWord item : words) {
                if (item == null || !StringUtils.hasText(item.getWord())) {
                    continue;
                }
                String hitWord = item.getWord().trim();
                if (hitWord.isEmpty()) {
                    continue;
                }
                if (text.contains(hitWord) || lowerText.contains(hitWord.toLowerCase(Locale.ROOT))) {
                    Integer level = item.getLevel() == null ? 2 : item.getLevel();
                    String key = hitWord.toLowerCase(Locale.ROOT) + "#" + level;
                    if (!dedup.add(key)) {
                        continue;
                    }
                    SensitiveHit hit = new SensitiveHit(
                            hitWord,
                            level,
                            item.getCategory(),
                            item.getHitActionDesc(),
                            item.getReasonTemplate());
                    if (level == SENSITIVE_LEVEL_REVIEW) {
                        reviewHits.add(hit);
                    } else {
                        blockedHits.add(hit);
                    }
                }
            }
        }
        return new SensitiveScanResult(blockedHits, reviewHits);
    }

    private void throwIfBlocked(String bizName, SensitiveScanResult result) {
        if (result == null || result.blockedHits().isEmpty()) {
            return;
        }
        SensitiveHit first = result.blockedHits().get(0);
        if (StringUtils.hasText(first.reasonTemplate())) {
            throw new BizException(ResultCode.BAD_REQUEST, first.reasonTemplate());
        }
        throw new BizException(ResultCode.BAD_REQUEST, bizName + " contains sensitive word: " + first.word());
    }

    private void createOrRefreshRuleAudit(Integer bizType,
            Long bizId,
            Long submitUserId,
            List<SensitiveHit> reviewHits) {
        if (bizType == null || bizId == null || reviewHits == null || reviewHits.isEmpty()) {
            return;
        }
        JsonNode hitDetail = objectMapper.valueToTree(reviewHits);
        CmsAudit exists = cmsAuditMapper.selectOne(new LambdaQueryWrapper<CmsAudit>()
                .eq(CmsAudit::getBizType, bizType)
                .eq(CmsAudit::getBizId, bizId)
                .eq(CmsAudit::getTriggerSource, AUDIT_TRIGGER_RULE)
                .eq(CmsAudit::getAuditStatus, 1)
                .last("LIMIT 1"));
        if (exists != null) {
            exists.setAuditType(AUDIT_TYPE_RULE);
            exists.setModelLabel("sensitive_rule");
            exists.setModelScore(null);
            exists.setHitDetail(hitDetail);
            exists.setSubmitUserId(submitUserId);
            cmsAuditMapper.updateById(exists);
            return;
        }
        CmsAudit audit = new CmsAudit();
        audit.setBizType(bizType);
        audit.setBizId(bizId);
        audit.setTriggerSource(AUDIT_TRIGGER_RULE);
        audit.setAuditType(AUDIT_TYPE_RULE);
        audit.setAuditStatus(1);
        audit.setAction(null);
        audit.setModelLabel("sensitive_rule");
        audit.setModelScore(null);
        audit.setHitDetail(hitDetail);
        audit.setRejectReason(null);
        audit.setSubmitUserId(submitUserId);
        cmsAuditMapper.insert(audit);
    }

    private record SensitiveScanResult(List<SensitiveHit> blockedHits, List<SensitiveHit> reviewHits) {
    }

    private record SensitiveHit(String word, Integer level, String category, String hitActionDesc,
            String reasonTemplate) {
    }

    private void fillImageUrls(AppQuestionDetailVO vo) {
        if (vo == null) {
            return;
        }
        vo.setImageUrls(parseImageUrls(vo.getImageUrlsRaw()));
    }

    private void fillImageUrls(AppQuestionListItemVO vo) {
        if (vo == null) {
            return;
        }
        vo.setImageUrls(parseImageUrls(vo.getImageUrlsRaw()));
    }

    private void fillImageUrls(AppMyQuestionItemVO vo) {
        if (vo == null) {
            return;
        }
        vo.setImageUrls(parseImageUrls(vo.getImageUrlsRaw()));
    }

    private void fillImageUrls(AppQuestionAnswerVO vo) {
        if (vo == null) {
            return;
        }
        vo.setImageUrls(parseImageUrls(vo.getImageUrlsRaw()));
    }

    private String serializeImageUrls(List<String> imageUrls) {
        List<String> normalized = normalizeImageUrls(imageUrls);
        if (normalized.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(normalized);
        } catch (Exception e) {
            throw new BizException(ResultCode.BAD_REQUEST, "invalid image urls");
        }
    }

    private List<String> parseImageUrls(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Collections.emptyList();
        }
        try {
            List<String> list = objectMapper.readValue(raw, new TypeReference<List<String>>() {
            });
            return normalizeImageUrls(list);
        } catch (Exception ignore) {
            return Collections.emptyList();
        }
    }

    private List<String> normalizeImageUrls(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return Collections.emptyList();
        }
        return imageUrls.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .limit(9)
                .toList();
    }

    private QaQuestion requirePublishedQuestion(Long questionId) {
        QaQuestion question = qaQuestionMapper.selectById(questionId);
        if (question == null || question.getDeleteFlag() == null || question.getDeleteFlag() != 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "question not found");
        }
        if (question.getStatus() == null || question.getStatus() != 1) {
            throw new BizException(ResultCode.BAD_REQUEST, "question is not published");
        }
        return question;
    }

    private boolean isQuestionLikedByUser(Long questionId, Long userId) {
        return qaVoteMapper.selectCount(new LambdaQueryWrapper<QaVote>()
                .eq(QaVote::getBizType, 1)
                .eq(QaVote::getBizId, questionId)
                .eq(QaVote::getUserId, userId)
                .eq(QaVote::getVoteType, 1)) > 0;
    }

    private boolean isQuestionFavoritedByUser(Long questionId, Long userId) {
        return qaFavoriteMapper.selectCount(new LambdaQueryWrapper<QaFavorite>()
                .eq(QaFavorite::getQuestionId, questionId)
                .eq(QaFavorite::getUserId, userId)) > 0;
    }

    private QaAnswer requirePublishedAnswer(Long answerId) {
        QaAnswer answer = qaAnswerMapper.selectById(answerId);
        if (answer == null || answer.getDeleteFlag() == null || answer.getDeleteFlag() != 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "answer not found");
        }
        if (answer.getStatus() == null || answer.getStatus() != 1) {
            throw new BizException(ResultCode.BAD_REQUEST, "answer is not published");
        }
        return answer;
    }

    private int countAnswerComments(Long answerId) {
        return Math.toIntExact(qaCommentMapper.selectCount(new LambdaQueryWrapper<QaComment>()
                .eq(QaComment::getBizType, 2)
                .eq(QaComment::getBizId, answerId)
                .eq(QaComment::getStatus, 1)
                .eq(QaComment::getDeleteFlag, 0)));
    }

    private int countAnswerFavorites(Long answerId) {
        return Math.toIntExact(qaVoteMapper.selectCount(new LambdaQueryWrapper<QaVote>()
                .eq(QaVote::getBizType, 2)
                .eq(QaVote::getBizId, answerId)
                .eq(QaVote::getVoteType, 2)));
    }

    private boolean isAnswerLikedByUser(Long answerId, Long userId) {
        return qaVoteMapper.selectCount(new LambdaQueryWrapper<QaVote>()
                .eq(QaVote::getBizType, 2)
                .eq(QaVote::getBizId, answerId)
                .eq(QaVote::getVoteType, 1)
                .eq(QaVote::getUserId, userId)) > 0;
    }

    private boolean isAnswerFavoritedByUser(Long answerId, Long userId) {
        return qaVoteMapper.selectCount(new LambdaQueryWrapper<QaVote>()
                .eq(QaVote::getBizType, 2)
                .eq(QaVote::getBizId, answerId)
                .eq(QaVote::getVoteType, 2)
                .eq(QaVote::getUserId, userId)) > 0;
    }

    private boolean insertAnswerVote(QaVote vote) {
        try {
            qaVoteMapper.insert(vote);
            return true;
        } catch (DuplicateKeyException ex) {
            QaVote sameVoteType = qaVoteMapper.selectOne(new LambdaQueryWrapper<QaVote>()
                    .eq(QaVote::getBizType, vote.getBizType())
                    .eq(QaVote::getBizId, vote.getBizId())
                    .eq(QaVote::getUserId, vote.getUserId())
                    .eq(QaVote::getVoteType, vote.getVoteType()));
            if (sameVoteType != null) {
                return false;
            }
            throw new BizException(ResultCode.BAD_REQUEST,
                    "点赞/收藏索引冲突，请先执行数据库脚本：20260319_fix_qa_vote_unique_key.sql");
        }
    }

    private Long resolveQuestionCategoryId(Long questionId) {
        if (questionId == null) {
            return null;
        }
        QaQuestion question = qaQuestionMapper.selectById(questionId);
        return question == null ? null : question.getCategoryId();
    }

    private boolean recordQuestionBrowseIfNeeded(Long userId, Long questionId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minusMinutes(Math.max(1L, viewDedupMinutes));

        UserBrowseHistory latest = userBrowseHistoryMapper.selectOne(new LambdaQueryWrapper<UserBrowseHistory>()
                .eq(UserBrowseHistory::getUserId, userId)
                .eq(UserBrowseHistory::getBizType, QUESTION_BROWSE_BIZ_TYPE)
                .eq(UserBrowseHistory::getBizId, questionId)
                .orderByDesc(UserBrowseHistory::getCreatedAt)
                .last("LIMIT 1"));

        if (latest != null) {
            boolean shouldCount = latest.getCreatedAt() == null || latest.getCreatedAt().isBefore(cutoff);
            latest.setCreatedAt(now);
            userBrowseHistoryMapper.updateById(latest);
            return shouldCount;
        }

        UserBrowseHistory browse = new UserBrowseHistory();
        browse.setUserId(userId);
        browse.setBizType(QUESTION_BROWSE_BIZ_TYPE);
        browse.setBizId(questionId);
        browse.setCreatedAt(now);
        userBrowseHistoryMapper.insert(browse);
        return true;
    }

    private Long requireUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser securityUser)) {
            throw new BizException(ResultCode.UNAUTHORIZED, "unauthorized");
        }
        return securityUser.getId();
    }

    private void assertUserCanPublish(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "user not found");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException(ResultCode.FORBIDDEN, "Account is disabled");
        }
        if (user.getBanUntil() != null && user.getBanUntil().isAfter(LocalDateTime.now())) {
            throw new BizException(ResultCode.FORBIDDEN,
                    "Account is temporarily restricted from posting until: " + user.getBanUntil());
        }
    }

    private void assertNoPendingDuplicateReport(Long reporterId, Integer bizType, Long bizId) {
        long pendingCount = cmsReportMapper.selectCount(new LambdaQueryWrapper<CmsReport>()
                .eq(CmsReport::getReporterId, reporterId)
                .eq(CmsReport::getBizType, bizType)
                .eq(CmsReport::getBizId, bizId)
                .eq(CmsReport::getStatus, CmsReport.STATUS_PENDING));
        if (pendingCount > 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "你的举报正在处理中，请勿重复提交。");
        }
    }

    private Integer resolveReasonType(String reasonCode) {
        if (!StringUtils.hasText(reasonCode)) {
            return 4;
        }
        String normalized = reasonCode.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "ad", "spam", "marketing" -> 1;
            case "illegal", "violence", "fraud" -> 2;
            case "abuse", "porn", "privacy", "misleading", "low_quality" -> 3;
            default -> 4;
        };
    }

    private void createNotifyIfNeeded(Long receiverId,
            Integer type,
            Integer bizType,
            Long bizId,
            String title,
            String content) {
        Long actorId = currentUserIdOrNull();
        if (receiverId == null || (actorId != null && receiverId.equals(actorId))) {
            return;
        }
        NotifyMessage notify = new NotifyMessage();
        notify.setReceiverId(receiverId);
        notify.setType(type);
        notify.setBizType(bizType);
        notify.setBizId(bizId);
        notify.setTitle(title);
        notify.setContent(content);
        notify.setIsRead(0);
        notifyMessageMapper.insert(notify);
    }

    private Long currentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser securityUser)) {
            return null;
        }
        return securityUser.getId();
    }

    private String actorName(Long userId) {
        if (userId == null) {
            return "用户";
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return "用户";
        }
        if (StringUtils.hasText(user.getNickname())) {
            return user.getNickname().trim();
        }
        if (StringUtils.hasText(user.getUsername())) {
            return user.getUsername().trim();
        }
        return "用户";
    }

    private String shorten(String text, int maxLen) {
        if (!StringUtils.hasText(text) || maxLen <= 0) {
            return "";
        }
        String trimmed = text.trim();
        if (trimmed.length() <= maxLen) {
            return trimmed;
        }
        return trimmed.substring(0, maxLen) + "...";
    }
}
