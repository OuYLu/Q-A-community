package com.community.service;

public interface RecommendationBehaviorService {
    void recordQuestionView(Long userId, Long questionId, Long categoryId);

    void recordQuestionLike(Long userId, Long questionId, Long categoryId, boolean liked);

    void recordQuestionFavorite(Long userId, Long questionId, Long categoryId, boolean favorited);

    void recordAnswerLike(Long userId, Long answerId, Long questionCategoryId, boolean liked);

    void recordAnswerFavorite(Long userId, Long answerId, Long questionCategoryId, boolean favorited);

    void recordKbView(Long userId, Long kbEntryId, Long kbCategoryId);

    void recordKbLike(Long userId, Long kbEntryId, Long kbCategoryId, boolean liked);

    void recordKbFavorite(Long userId, Long kbEntryId, Long kbCategoryId, boolean favorited);

    void recordSearch(Long userId, Integer hitCount);
}
