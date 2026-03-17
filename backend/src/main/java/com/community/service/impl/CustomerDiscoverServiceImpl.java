package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.SecurityUser;
import com.community.dto.AppQuestionPageQueryDTO;
import com.community.entity.UserPrivacySetting;
import com.community.mapper.ExpertProfileMapper;
import com.community.mapper.KbCategoryMapper;
import com.community.mapper.QaCategoryMapper;
import com.community.mapper.QaQuestionMapper;
import com.community.mapper.QaTopicMapper;
import com.community.mapper.UserPrivacySettingMapper;
import com.community.service.CustomerDiscoverService;
import com.community.vo.AppCategoryVO;
import com.community.vo.AppExpertCardVO;
import com.community.vo.AppGuestHomeVO;
import com.community.vo.AppKbCategoryVO;
import com.community.vo.AppQuestionListItemVO;
import com.community.vo.AppQuestionHotItemVO;
import com.community.vo.AppTopicListItemVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerDiscoverServiceImpl implements CustomerDiscoverService {
    private static final int CATEGORY_LIMIT_DEFAULT = 4;
    private static final int CATEGORY_LIMIT_MAX = 8;

    private final QaCategoryMapper qaCategoryMapper;
    private final KbCategoryMapper kbCategoryMapper;
    private final QaTopicMapper qaTopicMapper;
    private final QaQuestionMapper qaQuestionMapper;
    private final ExpertProfileMapper expertProfileMapper;
    private final UserPrivacySettingMapper userPrivacySettingMapper;

    @Override
    public AppGuestHomeVO guestHome(Integer topicLimit, Integer questionLimit, Integer expertLimit) {
        AppGuestHomeVO vo = new AppGuestHomeVO();
        Long userId = currentUserId();
        vo.setCategories(resolveQaCategories(userId, CATEGORY_LIMIT_DEFAULT));
        vo.setHotTopics(qaTopicMapper.selectAppHotTopics(resolveLimit(topicLimit, 8, 30)));
        vo.setHotQuestions(qaQuestionMapper.selectAppHotQuestions(resolveLimit(questionLimit, 8, 30)));
        vo.setExperts(expertProfileMapper.selectAppExpertCards(resolveLimit(expertLimit, 6, 20)));
        return vo;
    }

    @Override
    public PageInfo<AppQuestionListItemVO> questionPage(AppQuestionPageQueryDTO query) {
        int page = query == null || query.getPage() == null || query.getPage() <= 0 ? 1 : query.getPage();
        int pageSize = query == null || query.getPageSize() == null || query.getPageSize() <= 0 ? 10 : query.getPageSize();
        Long userId = currentUserId();
        boolean personalized = shouldPersonalize(query, userId);
        PageHelper.startPage(page, Math.min(pageSize, 50));
        List<AppQuestionListItemVO> rows = qaQuestionMapper.selectAppQuestionPage(
                query == null ? null : query.getKeyword(),
                query == null ? null : query.getCategoryId(),
                query == null ? null : query.getTopicId(),
                query == null ? null : query.getSortBy(),
                query == null ? null : query.getOnlyUnsolved(),
                userId,
                personalized
        );
        return new PageInfo<>(rows);
    }

    @Override
    public List<AppCategoryVO> listCategories(Integer limit) {
        return resolveQaCategories(currentUserId(), resolveCategoryLimit(limit));
    }

    @Override
    public List<AppCategoryVO> listAllCategories() {
        return qaCategoryMapper.selectAppCategoryList(null);
    }

    @Override
    public List<AppKbCategoryVO> listKbCategories(Integer limit) {
        Long userId = currentUserId();
        int resolvedLimit = resolveCategoryLimit(limit);
        if (userId != null && isPersonalizedRecommendEnabled(userId)) {
            List<AppKbCategoryVO> preferred = kbCategoryMapper.selectAppPreferredRootCategories(userId, resolvedLimit);
            if (preferred != null && !preferred.isEmpty()) {
                return preferred;
            }
        }
        return kbCategoryMapper.selectAppRootCategoryList(resolvedLimit);
    }

    @Override
    public List<AppTopicListItemVO> hotTopics(Integer limit) {
        int resolvedLimit = resolveLimit(limit, 10, 100);
        return qaTopicMapper.selectAppHotTopics(resolvedLimit);
    }

    @Override
    public List<AppQuestionHotItemVO> hotQuestions(Integer limit) {
        int resolvedLimit = resolveLimit(limit, 10, 100);
        return qaQuestionMapper.selectAppHotQuestions(resolvedLimit);
    }

    @Override
    public List<AppExpertCardVO> expertCards(Integer limit) {
        int resolvedLimit = resolveLimit(limit, 10, 100);
        return expertProfileMapper.selectAppExpertCards(resolvedLimit);
    }

    private int resolveLimit(Integer limit, int def, int max) {
        int resolved = (limit == null || limit <= 0) ? def : limit;
        return Math.min(resolved, max);
    }

    private int resolveCategoryLimit(Integer limit) {
        return resolveLimit(limit, CATEGORY_LIMIT_DEFAULT, CATEGORY_LIMIT_MAX);
    }

    private List<AppCategoryVO> resolveQaCategories(Long userId, int limit) {
        if (userId != null && isPersonalizedRecommendEnabled(userId)) {
            List<AppCategoryVO> preferred = qaCategoryMapper.selectAppPreferredCategories(userId, limit);
            if (preferred != null && !preferred.isEmpty()) {
                return preferred;
            }
        }
        return qaCategoryMapper.selectAppCategoryList(limit);
    }

    private boolean shouldPersonalize(AppQuestionPageQueryDTO query, Long userId) {
        if (userId == null || query == null) {
            return false;
        }
        if (!isPersonalizedRecommendEnabled(userId)) {
            return false;
        }
        if (StringUtils.hasText(query.getKeyword())) {
            return false;
        }
        String sortBy = StringUtils.hasText(query.getSortBy()) ? query.getSortBy().trim().toLowerCase() : "hot";
        return !"latest".equals(sortBy);
    }

    private boolean isPersonalizedRecommendEnabled(Long userId) {
        UserPrivacySetting setting = userPrivacySettingMapper.selectOne(new LambdaQueryWrapper<UserPrivacySetting>()
            .eq(UserPrivacySetting::getUserId, userId)
            .last("LIMIT 1"));
        if (setting == null || setting.getPersonalizedRecommend() == null) {
            return true;
        }
        return setting.getPersonalizedRecommend() == 1;
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser securityUser)) {
            return null;
        }
        return securityUser.getId();
    }
}
