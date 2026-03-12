package com.community.service.impl;

import com.community.entity.RecUserBehavior;
import com.community.mapper.RecUserBehaviorMapper;
import com.community.mapper.RecUserInterestMapper;
import com.community.service.RecommendationBehaviorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RecommendationBehaviorServiceImpl implements RecommendationBehaviorService {
    private static final int ACTION_VIEW = 1;
    private static final int ACTION_LIKE = 2;
    private static final int ACTION_FAVORITE = 3;
    private static final int ACTION_SEARCH = 5;

    private static final BigDecimal W_VIEW = new BigDecimal("1.00");
    private static final BigDecimal W_LIKE = new BigDecimal("3.00");
    private static final BigDecimal W_UNLIKE = new BigDecimal("-2.00");
    private static final BigDecimal W_FAVORITE = new BigDecimal("4.00");
    private static final BigDecimal W_UNFAVORITE = new BigDecimal("-3.00");
    private static final BigDecimal W_ANSWER_LIKE = new BigDecimal("2.00");
    private static final BigDecimal W_ANSWER_UNLIKE = new BigDecimal("-1.50");
    private static final BigDecimal W_ANSWER_FAVORITE = new BigDecimal("3.00");
    private static final BigDecimal W_ANSWER_UNFAVORITE = new BigDecimal("-2.00");
    private static final BigDecimal W_KB_VIEW = new BigDecimal("0.60");
    private static final BigDecimal W_KB_LIKE = new BigDecimal("1.20");
    private static final BigDecimal W_KB_UNLIKE = new BigDecimal("-0.80");
    private static final BigDecimal W_KB_FAVORITE = new BigDecimal("1.80");
    private static final BigDecimal W_KB_UNFAVORITE = new BigDecimal("-1.20");
    private static final BigDecimal W_SEARCH_BASE = new BigDecimal("0.50");

    private final RecUserBehaviorMapper recUserBehaviorMapper;
    private final RecUserInterestMapper recUserInterestMapper;

    @Override
    public void recordQuestionView(Long userId, Long questionId, Long categoryId) {
        record(userId, ACTION_VIEW, questionId, categoryId, W_VIEW);
    }

    @Override
    public void recordQuestionLike(Long userId, Long questionId, Long categoryId, boolean liked) {
        record(userId, ACTION_LIKE, questionId, categoryId, liked ? W_LIKE : W_UNLIKE);
    }

    @Override
    public void recordQuestionFavorite(Long userId, Long questionId, Long categoryId, boolean favorited) {
        record(userId, ACTION_FAVORITE, questionId, categoryId, favorited ? W_FAVORITE : W_UNFAVORITE);
    }

    @Override
    public void recordAnswerLike(Long userId, Long answerId, Long questionCategoryId, boolean liked) {
        record(userId, ACTION_LIKE, answerId, questionCategoryId, liked ? W_ANSWER_LIKE : W_ANSWER_UNLIKE);
    }

    @Override
    public void recordAnswerFavorite(Long userId, Long answerId, Long questionCategoryId, boolean favorited) {
        record(userId, ACTION_FAVORITE, answerId, questionCategoryId, favorited ? W_ANSWER_FAVORITE : W_ANSWER_UNFAVORITE);
    }

    @Override
    public void recordKbView(Long userId, Long kbEntryId, Long kbCategoryId) {
        record(userId, ACTION_VIEW, kbEntryId, kbCategoryId, W_KB_VIEW);
    }

    @Override
    public void recordKbLike(Long userId, Long kbEntryId, Long kbCategoryId, boolean liked) {
        record(userId, ACTION_LIKE, kbEntryId, kbCategoryId, liked ? W_KB_LIKE : W_KB_UNLIKE);
    }

    @Override
    public void recordKbFavorite(Long userId, Long kbEntryId, Long kbCategoryId, boolean favorited) {
        record(userId, ACTION_FAVORITE, kbEntryId, kbCategoryId, favorited ? W_KB_FAVORITE : W_KB_UNFAVORITE);
    }

    @Override
    public void recordSearch(Long userId, Integer hitCount) {
        BigDecimal bonus = BigDecimal.ZERO;
        if (hitCount != null && hitCount > 0) {
            int capped = Math.min(hitCount, 30);
            bonus = new BigDecimal(capped).multiply(new BigDecimal("0.03"));
        }
        record(userId, ACTION_SEARCH, 0L, null, W_SEARCH_BASE.add(bonus));
    }

    private void record(Long userId, Integer actionType, Long targetId, Long categoryId, BigDecimal weight) {
        if (userId == null || userId <= 0 || actionType == null || weight == null || weight.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        Long safeTargetId = (targetId == null || targetId < 0) ? 0L : targetId;
        try {
            RecUserBehavior behavior = new RecUserBehavior();
            behavior.setUserId(userId);
            behavior.setActionType(actionType);
            behavior.setTargetId(safeTargetId);
            behavior.setCategoryId(categoryId);
            behavior.setWeight(weight);
            behavior.setCreatedAt(LocalDateTime.now());
            recUserBehaviorMapper.insert(behavior);

            if (categoryId != null && categoryId > 0) {
                recUserInterestMapper.upsertInterestDelta(userId, categoryId, weight);
            }
        } catch (Exception ignored) {
            // Recommendation pipeline must not impact main business flow.
        }
    }
}
