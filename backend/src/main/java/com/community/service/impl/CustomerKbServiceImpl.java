package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.common.SecurityUser;
import com.community.dto.AppAnswerCommentCreateDTO;
import com.community.entity.CmsAudit;
import com.community.entity.CmsSensitiveWord;
import com.community.entity.KbEntry;
import com.community.entity.QaComment;
import com.community.entity.QaVote;
import com.community.entity.User;
import com.community.mapper.CmsAuditMapper;
import com.community.mapper.CmsSensitiveWordMapper;
import com.community.mapper.ExpertPostMapper;
import com.community.mapper.QaCommentMapper;
import com.community.mapper.QaVoteMapper;
import com.community.mapper.UserMapper;
import com.community.service.CustomerKbService;
import com.community.service.EsSearchService;
import com.community.service.RecommendationBehaviorService;
import com.community.vo.AppKbCommentVO;
import com.community.vo.AppKbInteractVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomerKbServiceImpl implements CustomerKbService {
    private static final int KB_STATUS_PUBLISHED = 1;
    private static final int KB_COMMENT_BIZ_TYPE = 4;
    private static final int KB_LIKE_BIZ_TYPE = 4;
    // Keep favorite in another biz type to avoid conflict with legacy unique key (biz_type,biz_id,user_id).
    private static final int KB_FAVORITE_BIZ_TYPE = 6;
    private static final int VOTE_TYPE_DEFAULT = 1;
    private static final int SENSITIVE_LEVEL_REVIEW = 1;
    private static final int AUDIT_TRIGGER_RULE = 1;
    private static final int AUDIT_TYPE_RULE = 1;
    private static final int AUDIT_BIZ_TYPE_COMMENT = 3;

    private final ExpertPostMapper expertPostMapper;
    private final QaVoteMapper qaVoteMapper;
    private final QaCommentMapper qaCommentMapper;
    private final UserMapper userMapper;
    private final CmsAuditMapper cmsAuditMapper;
    private final CmsSensitiveWordMapper sensitiveWordMapper;
    private final ObjectMapper objectMapper;
    private final EsSearchService esSearchService;
    private final RecommendationBehaviorService recommendationBehaviorService;

    @Override
    public AppKbInteractVO interaction(Long kbEntryId) {
        Long userId = requireUserId();
        KbEntry entry = requirePublishedKb(kbEntryId);
        return buildInteract(entry, userId);
    }

    @Override
    @Transactional
    public AppKbInteractVO toggleLike(Long kbEntryId) {
        Long userId = requireUserId();
        KbEntry entry = requirePublishedKb(kbEntryId);

        QaVote existed = qaVoteMapper.selectOne(new LambdaQueryWrapper<QaVote>()
            .eq(QaVote::getBizType, KB_LIKE_BIZ_TYPE)
            .eq(QaVote::getBizId, kbEntryId)
            .eq(QaVote::getUserId, userId)
            .eq(QaVote::getVoteType, VOTE_TYPE_DEFAULT)
            .last("LIMIT 1"));

        if (existed == null) {
            QaVote vote = new QaVote();
            vote.setBizType(KB_LIKE_BIZ_TYPE);
            vote.setBizId(kbEntryId);
            vote.setUserId(userId);
            vote.setVoteType(VOTE_TYPE_DEFAULT);
            qaVoteMapper.insert(vote);
            entry.setLikeCount((entry.getLikeCount() == null ? 0 : entry.getLikeCount()) + 1);
            recommendationBehaviorService.recordKbLike(userId, kbEntryId, entry.getCategoryId(), true);
        } else {
            qaVoteMapper.deleteById(existed.getId());
            int old = entry.getLikeCount() == null ? 0 : entry.getLikeCount();
            entry.setLikeCount(Math.max(0, old - 1));
            recommendationBehaviorService.recordKbLike(userId, kbEntryId, entry.getCategoryId(), false);
        }
        expertPostMapper.updateById(entry);
        syncKbForEs(kbEntryId);
        return buildInteract(entry, userId);
    }

    @Override
    @Transactional
    public AppKbInteractVO toggleFavorite(Long kbEntryId) {
        Long userId = requireUserId();
        KbEntry entry = requirePublishedKb(kbEntryId);

        QaVote existed = qaVoteMapper.selectOne(new LambdaQueryWrapper<QaVote>()
            .eq(QaVote::getBizType, KB_FAVORITE_BIZ_TYPE)
            .eq(QaVote::getBizId, kbEntryId)
            .eq(QaVote::getUserId, userId)
            .eq(QaVote::getVoteType, VOTE_TYPE_DEFAULT)
            .last("LIMIT 1"));

        if (existed == null) {
            QaVote vote = new QaVote();
            vote.setBizType(KB_FAVORITE_BIZ_TYPE);
            vote.setBizId(kbEntryId);
            vote.setUserId(userId);
            vote.setVoteType(VOTE_TYPE_DEFAULT);
            qaVoteMapper.insert(vote);
            entry.setFavoriteCount((entry.getFavoriteCount() == null ? 0 : entry.getFavoriteCount()) + 1);
            recommendationBehaviorService.recordKbFavorite(userId, kbEntryId, entry.getCategoryId(), true);
        } else {
            qaVoteMapper.deleteById(existed.getId());
            int old = entry.getFavoriteCount() == null ? 0 : entry.getFavoriteCount();
            entry.setFavoriteCount(Math.max(0, old - 1));
            recommendationBehaviorService.recordKbFavorite(userId, kbEntryId, entry.getCategoryId(), false);
        }
        expertPostMapper.updateById(entry);
        syncKbForEs(kbEntryId);
        return buildInteract(entry, userId);
    }

    @Override
    public List<AppKbCommentVO> comments(Long kbEntryId) {
        requireUserId();
        requirePublishedKb(kbEntryId);
        return qaCommentMapper.selectKbComments(kbEntryId);
    }

    @Override
    @Transactional
    public Long createComment(Long kbEntryId, AppAnswerCommentCreateDTO dto) {
        Long userId = requireUserId();
        assertUserCanPublish(userId);
        requirePublishedKb(kbEntryId);
        String content = dto == null ? null : dto.getContent();
        if (!StringUtils.hasText(content)) {
            throw new BizException(ResultCode.BAD_REQUEST, "comment content is required");
        }
        SensitiveScanResult sensitive = scanSensitiveWords(content);
        throwIfBlocked("comment", sensitive);

        Long parentId = dto == null ? null : dto.getParentId();
        if (parentId != null) {
            QaComment parent = qaCommentMapper.selectById(parentId);
            if (parent == null
                || parent.getDeleteFlag() == null || parent.getDeleteFlag() != 0
                || parent.getStatus() == null || parent.getStatus() != 1
                || parent.getBizType() == null || parent.getBizType() != KB_COMMENT_BIZ_TYPE
                || !kbEntryId.equals(parent.getBizId())) {
                throw new BizException(ResultCode.BAD_REQUEST, "parent comment not found");
            }
        }

        QaComment comment = new QaComment();
        comment.setBizType(KB_COMMENT_BIZ_TYPE);
        comment.setBizId(kbEntryId);
        comment.setUserId(userId);
        comment.setParentId(parentId);
        comment.setContent(content.trim());
        comment.setStatus(1);
        comment.setRejectReason(null);
        comment.setDeleteFlag(0);
        qaCommentMapper.insert(comment);
        createOrRefreshRuleAudit(AUDIT_BIZ_TYPE_COMMENT, comment.getId(), userId, sensitive.reviewHits());
        return comment.getId();
    }

    private AppKbInteractVO buildInteract(KbEntry entry, Long userId) {
        AppKbInteractVO vo = new AppKbInteractVO();
        vo.setKbEntryId(entry.getId());
        vo.setLikeCount(entry.getLikeCount() == null ? 0 : entry.getLikeCount());
        vo.setFavoriteCount(entry.getFavoriteCount() == null ? 0 : entry.getFavoriteCount());
        vo.setCommentCount(countComments(entry.getId()));
        vo.setLiked(hasVote(KB_LIKE_BIZ_TYPE, entry.getId(), userId));
        vo.setFavorited(hasVote(KB_FAVORITE_BIZ_TYPE, entry.getId(), userId));
        return vo;
    }

    private int countComments(Long kbEntryId) {
        return Math.toIntExact(qaCommentMapper.selectCount(new LambdaQueryWrapper<QaComment>()
            .eq(QaComment::getBizType, KB_COMMENT_BIZ_TYPE)
            .eq(QaComment::getBizId, kbEntryId)
            .eq(QaComment::getStatus, 1)
            .eq(QaComment::getDeleteFlag, 0)));
    }

    private boolean hasVote(Integer bizType, Long bizId, Long userId) {
        return qaVoteMapper.selectCount(new LambdaQueryWrapper<QaVote>()
            .eq(QaVote::getBizType, bizType)
            .eq(QaVote::getBizId, bizId)
            .eq(QaVote::getUserId, userId)
            .eq(QaVote::getVoteType, VOTE_TYPE_DEFAULT)) > 0;
    }

    private KbEntry requirePublishedKb(Long kbEntryId) {
        if (kbEntryId == null || kbEntryId <= 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "科普文章不存在");
        }
        KbEntry entry = expertPostMapper.selectById(kbEntryId);
        if (entry == null || entry.getStatus() == null || entry.getStatus() != KB_STATUS_PUBLISHED) {
            throw new BizException(ResultCode.BAD_REQUEST, "科普文章不存在或已下线");
        }
        return entry;
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

    private SensitiveScanResult scanSensitiveWords(String text) {
        if (!StringUtils.hasText(text)) {
            return new SensitiveScanResult(List.of(), List.of());
        }
        List<CmsSensitiveWord> words = sensitiveWordMapper.selectList(new LambdaQueryWrapper<CmsSensitiveWord>()
            .eq(CmsSensitiveWord::getEnabled, 1));
        if (words == null || words.isEmpty()) {
            return new SensitiveScanResult(List.of(), List.of());
        }
        String target = text.trim().toLowerCase(Locale.ROOT);
        List<SensitiveHit> blockedHits = new ArrayList<>();
        List<SensitiveHit> reviewHits = new ArrayList<>();
        Set<String> dedup = new LinkedHashSet<>();
        for (CmsSensitiveWord item : words) {
            if (item == null || !StringUtils.hasText(item.getWord())) {
                continue;
            }
            String word = item.getWord().trim();
            if (!StringUtils.hasText(word) || !target.contains(word.toLowerCase(Locale.ROOT))) {
                continue;
            }
            Integer level = item.getLevel() == null ? 2 : item.getLevel();
            String key = word.toLowerCase(Locale.ROOT) + "#" + level;
            if (!dedup.add(key)) {
                continue;
            }
            SensitiveHit hit = new SensitiveHit(
                word,
                level,
                item.getCategory(),
                item.getHitActionDesc(),
                item.getReasonTemplate()
            );
            if (level == SENSITIVE_LEVEL_REVIEW) {
                reviewHits.add(hit);
            } else {
                blockedHits.add(hit);
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

    private void syncKbForEs(Long kbEntryId) {
        if (esSearchService == null || !esSearchService.isEnabled() || kbEntryId == null) {
            return;
        }
        esSearchService.syncKbById(kbEntryId);
    }

    private record SensitiveScanResult(List<SensitiveHit> blockedHits, List<SensitiveHit> reviewHits) {
    }

    private record SensitiveHit(String word, Integer level, String category, String hitActionDesc, String reasonTemplate) {
    }
}
