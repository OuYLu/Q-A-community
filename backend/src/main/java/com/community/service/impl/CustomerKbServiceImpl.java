package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.common.SecurityUser;
import com.community.dto.AppAnswerCommentCreateDTO;
import com.community.entity.CmsSensitiveWord;
import com.community.entity.KbEntry;
import com.community.entity.QaComment;
import com.community.entity.QaVote;
import com.community.entity.User;
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
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerKbServiceImpl implements CustomerKbService {
    private static final int KB_STATUS_PUBLISHED = 1;
    private static final int KB_COMMENT_BIZ_TYPE = 4;
    private static final int KB_LIKE_BIZ_TYPE = 4;
    // Keep favorite in another biz type to avoid conflict with legacy unique key (biz_type,biz_id,user_id).
    private static final int KB_FAVORITE_BIZ_TYPE = 6;
    private static final int VOTE_TYPE_DEFAULT = 1;

    private final ExpertPostMapper expertPostMapper;
    private final QaVoteMapper qaVoteMapper;
    private final QaCommentMapper qaCommentMapper;
    private final UserMapper userMapper;
    private final CmsSensitiveWordMapper sensitiveWordMapper;
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
        validateNoSensitiveWords(dto == null ? null : dto.getContent());

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
        comment.setContent(dto.getContent().trim());
        comment.setStatus(1);
        comment.setRejectReason(null);
        comment.setDeleteFlag(0);
        qaCommentMapper.insert(comment);
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

    private void validateNoSensitiveWords(String text) {
        if (!StringUtils.hasText(text)) {
            throw new BizException(ResultCode.BAD_REQUEST, "comment content is required");
        }
        List<CmsSensitiveWord> words = sensitiveWordMapper.selectList(new LambdaQueryWrapper<CmsSensitiveWord>()
            .eq(CmsSensitiveWord::getEnabled, 1));
        if (words == null || words.isEmpty()) {
            return;
        }
        String target = text.trim().toLowerCase();
        for (CmsSensitiveWord item : words) {
            if (item == null || !StringUtils.hasText(item.getWord())) {
                continue;
            }
            String word = item.getWord().trim().toLowerCase();
            if (target.contains(word)) {
                throw new BizException(ResultCode.BAD_REQUEST, "评论包含敏感词：" + item.getWord());
            }
        }
    }

    private void syncKbForEs(Long kbEntryId) {
        if (esSearchService == null || !esSearchService.isEnabled() || kbEntryId == null) {
            return;
        }
        esSearchService.syncKbById(kbEntryId);
    }
}
