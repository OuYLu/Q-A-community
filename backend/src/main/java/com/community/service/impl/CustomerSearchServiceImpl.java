package com.community.service.impl;

import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.common.SecurityUser;
import com.community.dto.AppSearchLogDTO;
import com.community.dto.AppSearchQueryDTO;
import com.community.entity.SearchQueryLog;
import com.community.mapper.QaQuestionMapper;
import com.community.mapper.QaTagMapper;
import com.community.mapper.QaTopicMapper;
import com.community.mapper.SearchQueryLogMapper;
import com.community.service.CustomerSearchService;
import com.community.vo.AppSearchHistoryVO;
import com.community.vo.AppSearchHotVO;
import com.community.vo.AppSearchResultVO;
import com.community.vo.AppSearchTagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerSearchServiceImpl implements CustomerSearchService {
    private final QaQuestionMapper qaQuestionMapper;
    private final QaTopicMapper qaTopicMapper;
    private final QaTagMapper qaTagMapper;
    private final SearchQueryLogMapper searchQueryLogMapper;

    @Override
    public AppSearchResultVO search(AppSearchQueryDTO query) {
        if (query == null || !StringUtils.hasText(query.getQuery())) {
            throw new BizException(ResultCode.BAD_REQUEST, "查询词不能为空");
        }
        int page = query.getPage() == null || query.getPage() <= 0 ? 1 : query.getPage();
        int pageSize = query.getPageSize() == null || query.getPageSize() <= 0 ? 10 : query.getPageSize();
        int offset = (page - 1) * pageSize;
        String type = query.getType() == null ? "all" : query.getType().toLowerCase();

        AppSearchResultVO vo = new AppSearchResultVO();
        vo.setQuery(query.getQuery());

        if ("question".equals(type) || "all".equals(type)) {
            vo.setQuestions(qaQuestionMapper.selectAppSearchQuestions(query.getQuery(), pageSize, offset));
        } else {
            vo.setQuestions(Collections.emptyList());
        }
        if ("topic".equals(type) || "all".equals(type)) {
            vo.setTopics(qaTopicMapper.selectAppSearchTopics(query.getQuery(), 10));
        } else {
            vo.setTopics(Collections.emptyList());
        }
        if ("tag".equals(type) || "all".equals(type)) {
            List<AppSearchTagVO> tags = qaTagMapper.selectAppSearchTags(query.getQuery(), 10);
            vo.setTags(tags);
        } else {
            vo.setTags(Collections.emptyList());
        }
        return vo;
    }

    @Override
    public List<AppSearchHotVO> hot(Integer limit) {
        int resolved = resolveLimit(limit, 10, 50);
        return searchQueryLogMapper.selectHotQueries(resolved);
    }

    @Override
    public List<AppSearchHistoryVO> history(Integer limit) {
        Long userId = currentUserId();
        if (userId == null) {
            return Collections.emptyList();
        }
        int resolved = resolveLimit(limit, 10, 50);
        return searchQueryLogMapper.selectHistory(userId, resolved);
    }

    @Override
    @Transactional
    public void clearHistory() {
        Long userId = currentUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "未授权");
        }
        searchQueryLogMapper.deleteByUserId(userId);
    }

    @Override
    @Transactional
    public void logSearch(AppSearchLogDTO dto) {
        Long userId = currentUserId();
        SearchQueryLog log = new SearchQueryLog();
        log.setUserId(userId);
        log.setQueryText(dto.getQueryText());
        log.setSearchType(dto.getSearchType() == null ? 1 : dto.getSearchType());
        log.setHitCount(dto.getHitCount() == null ? 0 : dto.getHitCount());
        searchQueryLogMapper.insert(log);
    }

    private int resolveLimit(Integer limit, int def, int max) {
        int resolved = (limit == null || limit <= 0) ? def : limit;
        return Math.min(resolved, max);
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof SecurityUser securityUser) {
            return securityUser.getId();
        }
        return null;
    }
}
