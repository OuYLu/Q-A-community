package com.community.service.impl;

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
import org.springframework.stereotype.Service;

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
        vo.setCategories(qaCategoryMapper.selectAppCategoryList());
        vo.setHotTopics(qaTopicMapper.selectAppHotTopics(resolveLimit(topicLimit, 8, 30)));
        vo.setHotQuestions(qaQuestionMapper.selectAppHotQuestions(resolveLimit(questionLimit, 8, 30)));
        vo.setExperts(expertProfileMapper.selectAppExpertCards(resolveLimit(expertLimit, 6, 20)));
        return vo;
    }

    @Override
    public PageInfo<AppQuestionListItemVO> questionPage(AppQuestionPageQueryDTO query) {
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
}
