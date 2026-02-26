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
import com.community.dto.AppQuestionUpdateDTO;
import com.community.entity.CmsSensitiveWord;
import com.community.entity.QaAnswer;
import com.community.entity.QaCategory;
import com.community.entity.QaComment;
import com.community.entity.QaFavorite;
import com.community.entity.QaQuestion;
import com.community.entity.QaQuestionTag;
import com.community.entity.QaTag;
import com.community.entity.QaTopic;
import com.community.entity.QaVote;
import com.community.entity.UserStat;
import com.community.entity.UserBrowseHistory;
import com.community.mapper.QaAnswerMapper;
import com.community.mapper.QaCategoryMapper;
import com.community.mapper.CmsSensitiveWordMapper;
import com.community.mapper.QaCommentMapper;
import com.community.mapper.QaFavoriteMapper;
import com.community.mapper.QaQuestionMapper;
import com.community.mapper.QaQuestionTagMapper;
import com.community.mapper.QaTagMapper;
import com.community.mapper.QaTopicMapper;
import com.community.mapper.QaVoteMapper;
import com.community.mapper.UserBrowseHistoryMapper;
import com.community.mapper.UserStatMapper;
import com.community.service.CustomerQuestionService;
import com.community.vo.AppMyQuestionItemVO;
import com.community.vo.AppAnswerCommentVO;
import com.community.vo.AppAnswerDetailVO;
import com.community.vo.AppQuestionAnswerVO;
import com.community.vo.AppQuestionDetailVO;
import com.community.vo.AppQuestionListItemVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final CmsSensitiveWordMapper sensitiveWordMapper;
    private final UserBrowseHistoryMapper userBrowseHistoryMapper;
    private final UserStatMapper userStatMapper;
    private final ObjectMapper objectMapper;

    private static final int QUESTION_BROWSE_BIZ_TYPE = 1;

    @Value("${qa.view-dedup-minutes:5}")
    private long viewDedupMinutes;

    @Override
    public PageInfo<AppQuestionListItemVO> page(AppQuestionPageQueryDTO query) {
        int page = query == null || query.getPage() == null || query.getPage() <= 0 ? 1 : query.getPage();
        int pageSize = query == null || query.getPageSize() == null || query.getPageSize() <= 0 ? 10 : query.getPageSize();
        PageHelper.startPage(page, Math.min(pageSize, 50));
        List<AppQuestionListItemVO> rows = qaQuestionMapper.selectAppQuestionPage(
            query == null ? null : query.getKeyword(),
            query == null ? null : query.getCategoryId(),
            query == null ? null : query.getTopicId(),
            query == null ? null : query.getSortBy(),
            query == null ? null : query.getOnlyUnsolved()
        );
        rows.forEach(this::fillImageUrls);
        return new PageInfo<>(rows);
    }

    @Override
    public PageInfo<AppMyQuestionItemVO> myQuestions(AppPageQueryDTO query) {
        Long userId = requireUserId();
        int page = query == null || query.getPage() == null || query.getPage() <= 0 ? 1 : query.getPage();
        int pageSize = query == null || query.getPageSize() == null || query.getPageSize() <= 0 ? 10 : query.getPageSize();
        PageHelper.startPage(page, Math.min(pageSize, 50));
        List<AppMyQuestionItemVO> rows = qaQuestionMapper.selectMyQuestions(userId);
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
            answer.setCommentCount(countAnswerComments(answer.getId()));
            answer.setFavoriteCount(countAnswerFavorites(answer.getId()));
            answer.setLiked(isAnswerLikedByUser(answer.getId(), userId));
            answer.setFavorited(isAnswerFavoritedByUser(answer.getId(), userId));
            boolean isBest = question.getAcceptedAnswerId() != null && question.getAcceptedAnswerId().equals(answer.getId());
            answer.setBestAnswer(isBest);
            answer.setCanRecommend(userId.equals(question.getUserId()));
        }
        answers.sort(
            Comparator.comparing((AppQuestionAnswerVO a) -> Boolean.TRUE.equals(a.getBestAnswer()) ? 0 : 1)
                .thenComparing(AppQuestionAnswerVO::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(AppQuestionAnswerVO::getId, Comparator.nullsLast(Comparator.naturalOrder()))
        );
        vo.setAnswers(answers);
        return vo;
    }

    @Override
    @Transactional
    public Long createQuestion(AppQuestionCreateDTO dto) {
        Long userId = requireUserId();
        validateQuestionRef(dto.getCategoryId(), dto.getTopicId());
        validateNoSensitiveWords("问题", dto.getTitle(), dto.getContent());

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
        validateNoSensitiveWords("问题", dto.getTitle(), dto.getContent());

        Long oldTopicId = question.getTopicId();
        question.setTitle(dto.getTitle().trim());
        question.setContent(dto.getContent());
        question.setImageUrls(serializeImageUrls(dto.getImageUrls()));
        question.setCategoryId(dto.getCategoryId());
        question.setTopicId(dto.getTopicId());
        question.setLastActiveAt(LocalDateTime.now());
        qaQuestionMapper.updateById(question);

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

        qaQuestionMapper.update(null, new LambdaUpdateWrapper<QaQuestion>()
            .eq(QaQuestion::getId, id)
            .set(QaQuestion::getDeleteFlag, 1)
            .set(QaQuestion::getUpdatedAt, LocalDateTime.now()));
        increaseTopicQuestionCount(question.getTopicId(), -1);
        adjustUserQuestionCount(userId, -1);
    }

    @Override
    @Transactional
    public Long createAnswer(Long questionId, AppAnswerCreateDTO dto) {
        Long userId = requireUserId();
        QaQuestion question = qaQuestionMapper.selectById(questionId);
        if (question == null || question.getDeleteFlag() == null || question.getDeleteFlag() != 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "question not found");
        }
        if (question.getStatus() == null || question.getStatus() != 1) {
            throw new BizException(ResultCode.BAD_REQUEST, "question cannot be answered");
        }
        validateNoSensitiveWords("回答", dto.getContent());

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

        qaQuestionMapper.updateAnswerCount(questionId, 1);
        qaQuestionMapper.update(null, new LambdaUpdateWrapper<QaQuestion>()
            .eq(QaQuestion::getId, questionId)
            .set(QaQuestion::getLastActiveAt, LocalDateTime.now()));
        adjustUserAnswerCount(userId, 1);
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
        validateNoSensitiveWords("回答", dto.getContent());
        answer.setContent(dto.getContent().trim());
        answer.setImageUrls(serializeImageUrls(dto.getImageUrls()));
        qaAnswerMapper.updateById(answer);

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
        if (!userId.equals(answer.getUserId())) {
            throw new BizException(ResultCode.FORBIDDEN, "no permission to delete this answer");
        }

        qaAnswerMapper.update(null, new LambdaUpdateWrapper<QaAnswer>()
            .eq(QaAnswer::getId, answerId)
            .set(QaAnswer::getDeleteFlag, 1)
            .set(QaAnswer::getUpdatedAt, LocalDateTime.now()));

        qaQuestionMapper.updateAnswerCount(answer.getQuestionId(), -1);
        qaQuestionMapper.update(null, new LambdaUpdateWrapper<QaQuestion>()
            .eq(QaQuestion::getId, answer.getQuestionId())
            .set(QaQuestion::getLastActiveAt, LocalDateTime.now()));
        adjustUserAnswerCount(userId, -1);
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
        } else {
            qaVoteMapper.deleteById(existed.getId());
            int old = question.getLikeCount() == null ? 0 : question.getLikeCount();
            question.setLikeCount(Math.max(0, old - 1));
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
        } else {
            qaFavoriteMapper.deleteById(existed.getId());
            int old = question.getFavoriteCount() == null ? 0 : question.getFavoriteCount();
            question.setFavoriteCount(Math.max(0, old - 1));
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
        answer.setCommentCount(countAnswerComments(answer.getId()));
        answer.setFavoriteCount(countAnswerFavorites(answer.getId()));
        answer.setLiked(isAnswerLikedByUser(answer.getId(), userId));
        answer.setFavorited(isAnswerFavoritedByUser(answer.getId(), userId));
        boolean isBest = question.getAcceptedAnswerId() != null && question.getAcceptedAnswerId().equals(answer.getId());
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
            qaVoteMapper.insert(vote);
            answer.setLikeCount((answer.getLikeCount() == null ? 0 : answer.getLikeCount()) + 1);
        } else {
            qaVoteMapper.deleteById(existed.getId());
            int old = answer.getLikeCount() == null ? 0 : answer.getLikeCount();
            answer.setLikeCount(Math.max(0, old - 1));
        }
        qaAnswerMapper.updateById(answer);
        return answerDetail(answerId);
    }

    @Override
    @Transactional
    public AppAnswerDetailVO toggleAnswerFavorite(Long answerId) {
        Long userId = requireUserId();
        requirePublishedAnswer(answerId);

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
            qaVoteMapper.insert(vote);
        } else {
            qaVoteMapper.deleteById(existed.getId());
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
        if (answer == null || answer.getDeleteFlag() == null || answer.getDeleteFlag() != 0 || answer.getStatus() == null || answer.getStatus() != 1) {
            throw new BizException(ResultCode.BAD_REQUEST, "answer not found");
        }
        if (!questionId.equals(answer.getQuestionId())) {
            throw new BizException(ResultCode.BAD_REQUEST, "answer does not belong to this question");
        }

        boolean cancelBest = question.getAcceptedAnswerId() != null && question.getAcceptedAnswerId().equals(answerId);
        if (cancelBest) {
            question.setAcceptedAnswerId(null);
            question.setAcceptedAt(null);
        } else {
            question.setAcceptedAnswerId(answerId);
            question.setAcceptedAt(LocalDateTime.now());
        }
        qaQuestionMapper.updateById(question);
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
        requirePublishedAnswer(answerId);
        validateNoSensitiveWords("评论", dto.getContent());

        QaComment comment = new QaComment();
        comment.setBizType(2);
        comment.setBizId(answerId);
        comment.setUserId(userId);
        Long parentId = dto.getParentId();
        if (parentId != null) {
            QaComment parent = qaCommentMapper.selectById(parentId);
            if (parent == null
                || parent.getDeleteFlag() == null || parent.getDeleteFlag() != 0
                || parent.getStatus() == null || parent.getStatus() != 1
                || parent.getBizType() == null || parent.getBizType() != 2
                || !answerId.equals(parent.getBizId())) {
                throw new BizException(ResultCode.BAD_REQUEST, "parent comment not found");
            }
        }
        comment.setParentId(parentId);
        comment.setContent(dto.getContent().trim());
        comment.setStatus(1);
        comment.setRejectReason(null);
        comment.setDeleteFlag(0);
        qaCommentMapper.insert(comment);
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
        qaQuestionTagMapper.delete(new LambdaQueryWrapper<QaQuestionTag>()
            .eq(QaQuestionTag::getQuestionId, questionId));
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }

        Set<Long> uniqueTagIds = new LinkedHashSet<>(tagIds);
        List<QaTag> tags = qaTagMapper.selectBatchIds(uniqueTagIds);
        if (tags.size() != uniqueTagIds.size()) {
            throw new BizException(ResultCode.BAD_REQUEST, "tagIds contain invalid tag");
        }
        for (QaTag tag : tags) {
            if (tag.getStatus() == null || tag.getStatus() != 1) {
                throw new BizException(ResultCode.BAD_REQUEST, "tag is disabled");
            }
        }

        for (Long tagId : uniqueTagIds) {
            QaQuestionTag rel = new QaQuestionTag();
            rel.setQuestionId(questionId);
            rel.setTagId(tagId);
            qaQuestionTagMapper.insert(rel);
        }
    }

    private List<Long> resolveQuestionTagIds(List<Long> tagIds, List<String> tagNames) {
        LinkedHashSet<Long> result = new LinkedHashSet<>();
        if (tagIds != null) {
            for (Long id : tagIds) {
                if (id != null) {
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
                validateNoSensitiveWords("标签", name);
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
                    throw new BizException(ResultCode.BAD_REQUEST, bizName + "包含敏感词：" + hit);
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

    private boolean recordQuestionBrowseIfNeeded(Long userId, Long questionId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minusMinutes(Math.max(1L, viewDedupMinutes));

        UserBrowseHistory latest = userBrowseHistoryMapper.selectOne(new LambdaQueryWrapper<UserBrowseHistory>()
            .eq(UserBrowseHistory::getUserId, userId)
            .eq(UserBrowseHistory::getBizType, QUESTION_BROWSE_BIZ_TYPE)
            .eq(UserBrowseHistory::getBizId, questionId)
            .orderByDesc(UserBrowseHistory::getCreatedAt)
            .last("LIMIT 1"));

        if (latest != null && latest.getCreatedAt() != null && !latest.getCreatedAt().isBefore(cutoff)) {
            return false;
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
}
