package com.community.service.impl;

import com.community.common.SecurityUser;
import com.community.dto.AppQuestionPageQueryDTO;
import com.community.mapper.ExpertProfileMapper;
import com.community.mapper.QaCategoryMapper;
import com.community.mapper.QaQuestionMapper;
import com.community.mapper.QaTopicMapper;
import com.community.service.CustomerDiscoverService;
import com.community.vo.AppCategoryVO;
import com.community.vo.AppExpertCardVO;
import com.community.vo.AppGuestHomeVO;
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
    private final QaCategoryMapper qaCategoryMapper;
    private final QaTopicMapper qaTopicMapper;
    private final QaQuestionMapper qaQuestionMapper;
    private final ExpertProfileMapper expertProfileMapper;

    @Override
    public AppGuestHomeVO guestHome(Integer topicLimit, Integer questionLimit, Integer expertLimit) {
        AppGuestHomeVO vo = new AppGuestHomeVO();
        Long userId = currentUserId();
        List<AppCategoryVO> preferred = userId == null ? List.of() : qaCategoryMapper.selectAppPreferredCategoriesByInterest(userId, 2);
        if ((preferred == null || preferred.isEmpty()) && userId != null) {
            preferred = qaCategoryMapper.selectAppPreferredCategories(userId, 2);
        }
        if (preferred == null || preferred.isEmpty()) {
            List<AppCategoryVO> fallback = qaCategoryMapper.selectAppCategoryList();
            vo.setCategories(fallback == null ? List.of() : fallback.stream().limit(2).toList());
        } else {
            vo.setCategories(preferred);
        }
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
    public List<AppCategoryVO> listCategories() {
        return qaCategoryMapper.selectAppCategoryList();
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

    private boolean shouldPersonalize(AppQuestionPageQueryDTO query, Long userId) {
        if (userId == null || query == null) {
            return false;
        }
        if (StringUtils.hasText(query.getKeyword())) {
            return false;
        }
        String sortBy = StringUtils.hasText(query.getSortBy()) ? query.getSortBy().trim().toLowerCase() : "hot";
        return !"latest".equals(sortBy);
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser securityUser)) {
            return null;
        }
        return securityUser.getId();
    }
}
