package com.community.service.impl;

import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.common.SecurityUser;
import com.community.config.EsProperties;
import com.community.dto.AppSearchLogDTO;
import com.community.dto.AppSearchQueryDTO;
import com.community.entity.SearchQueryLog;
import com.community.mapper.KbEntryMapper;
import com.community.mapper.QaAnswerMapper;
import com.community.mapper.QaQuestionMapper;
import com.community.mapper.QaTagMapper;
import com.community.mapper.QaTopicMapper;
import com.community.mapper.SearchQueryLogMapper;
import com.community.service.CustomerSearchService;
import com.community.service.EsSearchService;
import com.community.vo.AppSearchAnswerVO;
import com.community.vo.AppSearchHistoryVO;
import com.community.vo.AppSearchHotVO;
import com.community.vo.AppSearchKbVO;
import com.community.vo.AppSearchQuestionVO;
import com.community.vo.AppSearchResultVO;
import com.community.vo.AppSearchSimilarQuestionVO;
import com.community.vo.AppSearchTagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CustomerSearchServiceImpl implements CustomerSearchService {
    private static final int SIMILAR_QUESTION_LIMIT = 5;
    private static final int HIGHLIGHT_TERM_LIMIT = 10;
    private static final Pattern ASCII_ALNUM = Pattern.compile("^[a-zA-Z0-9_-]+$");
    private static final String HIT_START = "<em class=\"search-hit\">";
    private static final String HIT_END = "</em>";

    private final QaQuestionMapper qaQuestionMapper;
    private final QaAnswerMapper qaAnswerMapper;
    private final QaTopicMapper qaTopicMapper;
    private final QaTagMapper qaTagMapper;
    private final KbEntryMapper kbEntryMapper;
    private final SearchQueryLogMapper searchQueryLogMapper;
    private final EsSearchService esSearchService;
    private final EsProperties esProperties;

    @Override
    public AppSearchResultVO search(AppSearchQueryDTO query) {
        if (query == null || !StringUtils.hasText(query.getQuery())) {
            throw new BizException(ResultCode.BAD_REQUEST, "查询词不能为空");
        }
        int page = query.getPage() == null || query.getPage() <= 0 ? 1 : query.getPage();
        int pageSize = query.getPageSize() == null || query.getPageSize() <= 0 ? 10 : query.getPageSize();
        int offset = (page - 1) * pageSize;
        String type = query.getType() == null ? "all" : query.getType().toLowerCase(Locale.ROOT);
        List<String> highlightKeywords = buildHighlightKeywords(query.getQuery());

        AppSearchResultVO vo = new AppSearchResultVO();
        vo.setQuery(query.getQuery());

        if ("question".equals(type) || "all".equals(type)) {
            vo.setQuestions(searchQuestions(query, pageSize, offset, highlightKeywords));
            vo.setSimilarQuestions(searchSimilarQuestions(query, highlightKeywords));
        } else {
            vo.setQuestions(Collections.emptyList());
            vo.setSimilarQuestions(Collections.emptyList());
        }

        if ("answer".equals(type) || "all".equals(type)) {
            vo.setAnswers(searchAnswers(query, pageSize, offset, highlightKeywords));
        } else {
            vo.setAnswers(Collections.emptyList());
        }

        if ("kb".equals(type) || "all".equals(type)) {
            vo.setKbEntries(searchKb(query, pageSize, offset, highlightKeywords));
        } else {
            vo.setKbEntries(Collections.emptyList());
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

    private List<AppSearchQuestionVO> searchQuestions(AppSearchQueryDTO query, int pageSize, int offset,
                                                      List<String> highlightKeywords) {
        if (useEsSearch()) {
            try {
                List<Long> ids = esSearchService.searchQuestionIds(
                    query.getQuery(),
                    offset,
                    pageSize,
                    query.getCategoryId(),
                    query.getTopicId(),
                    query.getOnlyUnsolved(),
                    query.getSortBy()
                );
                if (ids != null && !ids.isEmpty()) {
                    List<AppSearchQuestionVO> rows = qaQuestionMapper.selectAppSearchQuestionsByIds(
                        ids,
                        query.getCategoryId(),
                        query.getTopicId(),
                        query.getOnlyUnsolved()
                    );
                    applyQuestionHighlights(rows, highlightKeywords);
                    return rows;
                }
                List<AppSearchQuestionVO> rows = qaQuestionMapper.selectAppSearchQuestions(
                    query.getQuery(),
                    query.getSortBy(),
                    query.getCategoryId(),
                    query.getTopicId(),
                    query.getOnlyUnsolved(),
                    pageSize,
                    offset
                );
                applyQuestionHighlights(rows, highlightKeywords);
                return rows;
            } catch (Exception ignored) {
                // fall back to mysql
            }
        }
        List<AppSearchQuestionVO> rows = qaQuestionMapper.selectAppSearchQuestions(
            query.getQuery(),
            query.getSortBy(),
            query.getCategoryId(),
            query.getTopicId(),
            query.getOnlyUnsolved(),
            pageSize,
            offset
        );
        applyQuestionHighlights(rows, highlightKeywords);
        return rows;
    }

    private List<AppSearchKbVO> searchKb(AppSearchQueryDTO query, int pageSize, int offset,
                                         List<String> highlightKeywords) {
        if (useEsSearch()) {
            try {
                List<Long> ids = esSearchService.searchKbIds(query.getQuery(), offset, pageSize);
                if (ids != null && !ids.isEmpty()) {
                    List<AppSearchKbVO> rows = kbEntryMapper.selectAppSearchKbByIds(ids);
                    applyKbHighlights(rows, highlightKeywords);
                    return rows;
                }
                List<AppSearchKbVO> rows = kbEntryMapper.selectAppSearchKb(query.getQuery(), pageSize, offset);
                applyKbHighlights(rows, highlightKeywords);
                return rows;
            } catch (Exception ignored) {
                // fall back to mysql
            }
        }
        List<AppSearchKbVO> rows = kbEntryMapper.selectAppSearchKb(query.getQuery(), pageSize, offset);
        applyKbHighlights(rows, highlightKeywords);
        return rows;
    }

    private List<AppSearchAnswerVO> searchAnswers(AppSearchQueryDTO query, int pageSize, int offset,
                                                  List<String> highlightKeywords) {
        List<String> semanticTerms = Collections.emptyList();
        try {
            semanticTerms = esSearchService.buildSemanticTerms(query.getQuery(), 8);
        } catch (Exception ignored) {
            // keep lexical fallback for answers
        }
        List<AppSearchAnswerVO> rows = qaAnswerMapper.selectAppSearchAnswers(
            query.getQuery(),
            semanticTerms,
            query.getSortBy(),
            pageSize,
            offset
        );
        applyAnswerHighlights(rows, highlightKeywords);
        return rows;
    }

    private List<AppSearchSimilarQuestionVO> searchSimilarQuestions(AppSearchQueryDTO query, List<String> highlightKeywords) {
        List<AppSearchQuestionVO> candidates = Collections.emptyList();
        if (useEsSearch()) {
            try {
                List<Long> ids = esSearchService.searchQuestionIds(
                    query.getQuery(),
                    0,
                    SIMILAR_QUESTION_LIMIT * 2,
                    query.getCategoryId(),
                    query.getTopicId(),
                    query.getOnlyUnsolved(),
                    "comprehensive"
                );
                if (ids != null && !ids.isEmpty()) {
                    candidates = qaQuestionMapper.selectAppSearchQuestionsByIds(
                        ids,
                        query.getCategoryId(),
                        query.getTopicId(),
                        query.getOnlyUnsolved()
                    );
                }
            } catch (Exception ignored) {
                // fallback to mysql below
            }
        }

        if (candidates == null || candidates.isEmpty()) {
            candidates = qaQuestionMapper.selectAppSearchQuestions(
                query.getQuery(),
                "comprehensive",
                query.getCategoryId(),
                query.getTopicId(),
                query.getOnlyUnsolved(),
                SIMILAR_QUESTION_LIMIT * 2,
                0
            );
        }
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }

        String queryNorm = normalizeText(query.getQuery());
        Set<String> dedup = new LinkedHashSet<>();
        List<AppSearchSimilarQuestionVO> result = new ArrayList<>();
        for (AppSearchQuestionVO candidate : candidates) {
            if (candidate == null || !StringUtils.hasText(candidate.getTitle()) || candidate.getId() == null) {
                continue;
            }
            String title = candidate.getTitle().trim();
            String norm = normalizeText(title);
            if (!StringUtils.hasText(norm) || norm.equals(queryNorm) || !dedup.add(norm)) {
                continue;
            }
            AppSearchSimilarQuestionVO suggestion = new AppSearchSimilarQuestionVO();
            suggestion.setId(candidate.getId());
            suggestion.setTitle(title);
            suggestion.setTitleHighlight(highlightText(title, highlightKeywords));
            result.add(suggestion);
            if (result.size() >= SIMILAR_QUESTION_LIMIT) {
                break;
            }
        }
        return result;
    }

    private List<String> buildHighlightKeywords(String query) {
        if (!StringUtils.hasText(query)) {
            return Collections.emptyList();
        }
        LinkedHashSet<String> keywords = new LinkedHashSet<>();
        String normalizedQuery = normalizeText(query);
        if (StringUtils.hasText(normalizedQuery)) {
            keywords.add(normalizedQuery);
            for (String token : normalizedQuery.split("\\s+")) {
                if (StringUtils.hasText(token)) {
                    keywords.add(token);
                }
            }
        }
        try {
            List<String> semanticTerms = esSearchService.buildSemanticTerms(query, HIGHLIGHT_TERM_LIMIT);
            if (semanticTerms != null) {
                for (String term : semanticTerms) {
                    String normalized = normalizeText(term);
                    if (StringUtils.hasText(normalized)) {
                        keywords.add(normalized);
                    }
                }
            }
        } catch (Exception ignored) {
            // keep lexical highlight only
        }
        List<String> result = new ArrayList<>(keywords);
        result.sort(Comparator.comparingInt(String::length).reversed());
        if (result.size() > HIGHLIGHT_TERM_LIMIT) {
            return result.subList(0, HIGHLIGHT_TERM_LIMIT);
        }
        return result;
    }

    private void applyQuestionHighlights(List<AppSearchQuestionVO> rows, List<String> keywords) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        for (AppSearchQuestionVO row : rows) {
            if (row == null) {
                continue;
            }
            row.setTitleHighlight(highlightText(row.getTitle(), keywords));
            row.setSummaryHighlight(highlightText(row.getSummary(), keywords));
        }
    }

    private void applyKbHighlights(List<AppSearchKbVO> rows, List<String> keywords) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        for (AppSearchKbVO row : rows) {
            if (row == null) {
                continue;
            }
            row.setTitleHighlight(highlightText(row.getTitle(), keywords));
            row.setSummaryHighlight(highlightText(row.getSummary(), keywords));
        }
    }

    private void applyAnswerHighlights(List<AppSearchAnswerVO> rows, List<String> keywords) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        for (AppSearchAnswerVO row : rows) {
            if (row == null) {
                continue;
            }
            row.setQuestionTitleHighlight(highlightText(row.getQuestionTitle(), keywords));
            row.setContentPreviewHighlight(highlightText(row.getContentPreview(), keywords));
        }
    }

    private String highlightText(String raw, List<String> keywords) {
        if (raw == null) {
            return "";
        }
        String escaped = escapeHtml(raw);
        if (keywords == null || keywords.isEmpty()) {
            return escaped;
        }
        String highlighted = escaped;
        for (String keyword : keywords) {
            if (!StringUtils.hasText(keyword)) {
                continue;
            }
            highlighted = highlightKeyword(highlighted, keyword.trim());
        }
        return highlighted;
    }

    private String highlightKeyword(String input, String keyword) {
        if (!StringUtils.hasText(input) || !StringUtils.hasText(keyword)) {
            return input;
        }
        String escapedKeyword = escapeHtml(keyword);
        if (!StringUtils.hasText(escapedKeyword)) {
            return input;
        }
        Pattern pattern = isAscii(escapedKeyword)
            ? Pattern.compile(Pattern.quote(escapedKeyword), Pattern.CASE_INSENSITIVE)
            : Pattern.compile(Pattern.quote(escapedKeyword));
        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String matched = matcher.group();
            matcher.appendReplacement(sb, Matcher.quoteReplacement(HIT_START + matched + HIT_END));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private boolean isAscii(String text) {
        return StringUtils.hasText(text) && ASCII_ALNUM.matcher(text).matches();
    }

    private String normalizeText(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        return text.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private String escapeHtml(String raw) {
        if (raw == null) {
            return "";
        }
        return raw
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    private boolean useEsSearch() {
        if (esSearchService == null || !esSearchService.isEnabled()) {
            return false;
        }
        String strategy = esProperties == null ? null : esProperties.getSearchStrategy();
        return strategy == null || !"mysql".equalsIgnoreCase(strategy.trim());
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
