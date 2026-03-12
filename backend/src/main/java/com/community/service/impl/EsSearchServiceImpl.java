package com.community.service.impl;

import com.community.config.EsProperties;
import com.community.entity.KbEntry;
import com.community.entity.QaQuestion;
import com.community.mapper.KbEntryMapper;
import com.community.mapper.QaQuestionMapper;
import com.community.service.EsSearchService;
import com.community.vo.SearchKbDoc;
import com.community.vo.SearchQuestionDoc;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class EsSearchServiceImpl implements EsSearchService {
    private static final DateTimeFormatter ES_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String READ_ALIAS_SUFFIX = "_read";
    private static final String WRITE_ALIAS_SUFFIX = "_write";
    private static final String INITIAL_VERSION_SUFFIX = "_v1";
    private static final int SEMANTIC_VARIANT_LIMIT = 8;
    private static final int SEMANTIC_TERM_LIMIT = 12;
    private static final Map<String, List<String>> MEDICAL_SYNONYMS = Map.ofEntries(
        Map.entry("\u9ad8\u8840\u538b", List.of("\u8840\u538b\u9ad8", "hypertension")),
        Map.entry("\u7cd6\u5c3f\u75c5", List.of("\u8840\u7cd6\u9ad8", "diabetes")),
        Map.entry("\u8111\u68d7", List.of("\u8111\u68d7\u585e", "\u8111\u68d7\u6b7b")),
        Map.entry("\u4e2d\u98ce", List.of("\u5352\u4e2d")),
        Map.entry("\u53d1\u70e7", List.of("\u53d1\u70ed")),
        Map.entry("\u611f\u5192", List.of("\u4e0a\u547c\u5438\u9053\u611f\u67d3")),
        Map.entry("\u5934\u6655", List.of("\u7729\u6655")),
        Map.entry("\u80c3\u75bc", List.of("\u80c3\u75db")),
        Map.entry("\u5931\u7720", List.of("\u7761\u4e0d\u7740", "\u7761\u7720\u969c\u788d")),
        Map.entry("\u4fbf\u79d8", List.of("\u6392\u4fbf\u56f0\u96be"))
    );

    private static final int MIN_SEMANTIC_LIMIT = 1;
    private static final int MAX_SEMANTIC_TERM_LIMIT = 24;
    private static final int MAX_SEMANTIC_VARIANT_LIMIT = 20;
    private static final Pattern ASCII_ALNUM = Pattern.compile("^[a-zA-Z0-9_-]+$");

    private final EsProperties properties;
    private final RestTemplateBuilder restTemplateBuilder;
    private final ObjectMapper objectMapper;
    private final QaQuestionMapper qaQuestionMapper;
    private final KbEntryMapper kbEntryMapper;

    private RestTemplate restTemplate;
    private volatile IndexRoute questionRoute;
    private volatile IndexRoute kbRoute;

    @Override
    public boolean isEnabled() {
        return properties != null && properties.isEnabled() && StringUtils.hasText(properties.getBaseUrl());
    }

    @Override
    public void prepareIndices() {
        if (!isEnabled()) {
            return;
        }
        questionRoute = ensureIndexRoute(properties.getIndexQuestion(), questionMapping());
        kbRoute = ensureIndexRoute(properties.getIndexKb(), kbMapping());
    }

    @Override
    public void syncQuestionById(Long questionId) {
        if (!isEnabled() || questionId == null) {
            return;
        }
        IndexRoute route = getQuestionRoute();
        if (route == null) {
            return;
        }
        QaQuestion row = qaQuestionMapper.selectById(questionId);
        if (row == null
            || row.getDeleteFlag() == null || row.getDeleteFlag() != 0
            || row.getStatus() == null || row.getStatus() != QaQuestion.STATUS_PUBLISHED) {
            deleteDoc(route.writeAlias(), questionId);
            return;
        }
        SearchQuestionDoc doc = qaQuestionMapper.selectSearchQuestionDocById(questionId);
        if (doc == null) {
            deleteDoc(route.writeAlias(), questionId);
            return;
        }
        indexQuestion(doc);
    }

    @Override
    public void syncKbById(Long kbId) {
        if (!isEnabled() || kbId == null) {
            return;
        }
        IndexRoute route = getKbRoute();
        if (route == null) {
            return;
        }
        KbEntry row = kbEntryMapper.selectById(kbId);
        if (row == null || row.getStatus() == null || row.getStatus() != 1) {
            deleteDoc(route.writeAlias(), kbId);
            return;
        }
        SearchKbDoc doc = new SearchKbDoc();
        doc.setId(row.getId());
        doc.setTitle(row.getTitle());
        doc.setSummary(row.getSummary());
        doc.setContent(row.getContent());
        doc.setSource(row.getSource());
        doc.setViewCount(row.getViewCount());
        doc.setLikeCount(row.getLikeCount());
        doc.setFavoriteCount(row.getFavoriteCount());
        doc.setStatus(row.getStatus());
        doc.setCreatedAt(row.getCreatedAt());
        indexKb(doc);
    }

    @Override
    public List<Long> searchQuestionIds(String query, int from, int size, Long categoryId, Long topicId, Boolean onlyUnsolved, String sortBy) {
        if (!isEnabled() || !StringUtils.hasText(query)) {
            return List.of();
        }
        IndexRoute route = getQuestionRoute();
        if (route == null) {
            return List.of();
        }
        Map<String, Object> body = buildQuestionSearchBody(query, from, size, categoryId, topicId, onlyUnsolved, sortBy);
        return executeSearch(route.readAlias(), body);
    }

    @Override
    public List<Long> searchKbIds(String query, int from, int size) {
        if (!isEnabled() || !StringUtils.hasText(query)) {
            return List.of();
        }
        IndexRoute route = getKbRoute();
        if (route == null) {
            return List.of();
        }
        Map<String, Object> body = buildKbSearchBody(query, from, size);
        return executeSearch(route.readAlias(), body);
    }

    @Override
    public List<String> buildSemanticTerms(String query, int limit) {
        if (!StringUtils.hasText(query)) {
            return List.of();
        }
        int resolvedLimit = Math.max(MIN_SEMANTIC_LIMIT, limit);
        List<String> terms = extractSemanticTerms(query);
        LinkedHashSet<String> expanded = new LinkedHashSet<>(terms);
        for (String variant : expandSemanticVariants(query, terms)) {
            if (!StringUtils.hasText(variant)) {
                continue;
            }
            String[] parts = variant.split("\\s+");
            for (String part : parts) {
                if (StringUtils.hasText(part)) {
                    expanded.add(part);
                }
                if (expanded.size() >= resolvedLimit) {
                    break;
                }
            }
            if (expanded.size() >= resolvedLimit) {
                break;
            }
        }
        String normalized = normalizeQuery(query);
        expanded.remove(normalized);
        List<String> result = new ArrayList<>(expanded);
        return result.subList(0, Math.min(resolvedLimit, result.size()));
    }

    @Override
    public void reindexAll() {
        if (!isEnabled()) {
            return;
        }
        prepareIndices();
        IndexRoute question = getQuestionRoute();
        IndexRoute kb = getKbRoute();
        if (question == null || kb == null) {
            return;
        }

        List<SearchQuestionDoc> questions = qaQuestionMapper.selectSearchQuestionDocs();
        if (!CollectionUtils.isEmpty(questions)) {
            bulkIndex(question.writeAlias(), buildQuestionBulk(questions));
        }

        List<SearchKbDoc> kbs = kbEntryMapper.selectSearchKbDocs();
        if (!CollectionUtils.isEmpty(kbs)) {
            bulkIndex(kb.writeAlias(), buildKbBulk(kbs));
        }
    }

    @Override
    public void indexQuestion(SearchQuestionDoc doc) {
        if (!isEnabled() || doc == null || doc.getId() == null) {
            return;
        }
        IndexRoute route = getQuestionRoute();
        if (route == null) {
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", doc.getId());
        payload.put("title", safe(doc.getTitle()));
        payload.put("content", safe(doc.getContent()));
        payload.put("answerContent", safe(doc.getAnswerContent()));
        payload.put("categoryId", doc.getCategoryId());
        payload.put("topicId", doc.getTopicId());
        payload.put("answerCount", doc.getAnswerCount() == null ? 0 : doc.getAnswerCount());
        payload.put("viewCount", doc.getViewCount() == null ? 0 : doc.getViewCount());
        payload.put("likeCount", doc.getLikeCount() == null ? 0 : doc.getLikeCount());
        payload.put("favoriteCount", doc.getFavoriteCount() == null ? 0 : doc.getFavoriteCount());
        payload.put("hotScore", questionHotScore(doc));
        payload.put("status", doc.getStatus() == null ? 1 : doc.getStatus());
        payload.put("createdAt", formatTime(doc.getCreatedAt()));
        indexDoc(route.writeAlias(), doc.getId(), payload);
    }

    @Override
    public void indexKb(SearchKbDoc doc) {
        if (!isEnabled() || doc == null || doc.getId() == null) {
            return;
        }
        IndexRoute route = getKbRoute();
        if (route == null) {
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", doc.getId());
        payload.put("title", safe(doc.getTitle()));
        payload.put("summary", safe(doc.getSummary()));
        payload.put("content", safe(doc.getContent()));
        payload.put("source", safe(doc.getSource()));
        payload.put("viewCount", doc.getViewCount() == null ? 0 : doc.getViewCount());
        payload.put("likeCount", doc.getLikeCount() == null ? 0 : doc.getLikeCount());
        payload.put("favoriteCount", doc.getFavoriteCount() == null ? 0 : doc.getFavoriteCount());
        payload.put("hotScore", kbHotScore(doc));
        payload.put("status", doc.getStatus() == null ? 1 : doc.getStatus());
        payload.put("createdAt", formatTime(doc.getCreatedAt()));
        indexDoc(route.writeAlias(), doc.getId(), payload);
    }

    private void indexDoc(String index, Long id, Map<String, Object> payload) {
        String url = buildUrl("/" + index + "/_doc/" + id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        restTemplate().exchange(url, HttpMethod.PUT, entity, String.class);
    }

    private void deleteDoc(String index, Long id) {
        String url = buildUrl("/" + index + "/_doc/" + id);
        try {
            restTemplate().exchange(url, HttpMethod.DELETE, null, String.class);
        } catch (HttpClientErrorException.NotFound ignored) {
            // doc already absent
        }
    }

    private Map<String, Object> buildQuestionSearchBody(String query, int from, int size,
                                                        Long categoryId, Long topicId, Boolean onlyUnsolved,
                                                        String sortBy) {
        String resolvedSortBy = StringUtils.hasText(sortBy) ? sortBy.trim().toLowerCase() : "comprehensive";
        String strategy = resolveSearchStrategy();
        boolean semanticMode = "es_hybrid".equals(strategy);
        Map<String, Object> baseQuery = questionBaseQuery(query, categoryId, topicId, onlyUnsolved, semanticMode);
        boolean hybrid = semanticMode && "comprehensive".equals(resolvedSortBy);

        Map<String, Object> body = new HashMap<>();
        body.put("from", Math.max(from, 0));
        body.put("size", Math.max(size, 1));
        body.put("query", hybrid ? questionHybridQuery(baseQuery) : baseQuery);
        body.put("sort", questionSort(resolvedSortBy, hybrid));
        return body;
    }

    private Map<String, Object> buildKbSearchBody(String query, int from, int size) {
        String strategy = resolveSearchStrategy();
        boolean semanticMode = "es_hybrid".equals(strategy);
        Map<String, Object> bool = kbBaseQuery(query, semanticMode);

        Map<String, Object> body = new HashMap<>();
        body.put("from", Math.max(from, 0));
        body.put("size", Math.max(size, 1));
        body.put("query", semanticMode ? kbHybridQuery(bool) : bool);
        body.put("sort", List.of(
            Map.of("_score", "desc"),
            Map.of("createdAt", "desc"),
            Map.of("id", "desc")
        ));
        return body;
    }

    private Map<String, Object> questionBaseQuery(String query, Long categoryId, Long topicId,
                                                  Boolean onlyUnsolved, boolean semanticMode) {
        List<Object> must = new ArrayList<>();
        must.add(semanticMode ? questionSemanticClause(query) : Map.of("multi_match", Map.of(
            "query", query,
            "fields", List.of("title^4", "content", "answerContent^1.2")
        )));

        List<Object> filters = new ArrayList<>();
        filters.add(Map.of("term", Map.of("status", 1)));
        if (categoryId != null) {
            filters.add(Map.of("term", Map.of("categoryId", categoryId)));
        }
        if (topicId != null) {
            filters.add(Map.of("term", Map.of("topicId", topicId)));
        }
        if (onlyUnsolved != null && onlyUnsolved) {
            filters.add(Map.of("term", Map.of("answerCount", 0)));
        }

        Map<String, Object> bool = new HashMap<>();
        bool.put("must", must);
        bool.put("filter", filters);
        return Map.of("bool", bool);
    }

    private Map<String, Object> kbBaseQuery(String query, boolean semanticMode) {
        Map<String, Object> bool = new HashMap<>();
        bool.put("must", List.of(semanticMode ? kbSemanticClause(query) : Map.of("multi_match", Map.of(
            "query", query,
            "fields", List.of("title^3", "summary^2", "content")
        ))));
        bool.put("filter", List.of(Map.of("term", Map.of("status", 1))));
        return Map.of("bool", bool);
    }

    private Map<String, Object> questionSemanticClause(String query) {
        String normalizedQuery = normalizeQuery(query);
        if (!StringUtils.hasText(normalizedQuery)) {
            normalizedQuery = query.trim();
        }
        List<Object> must = new ArrayList<>();
        must.add(coreQueryClause(
            normalizedQuery,
            List.of("title^6", "content^3", "answerContent^2")
        ));
        List<Object> should = new ArrayList<>();
        should.add(multiMatchClause(normalizedQuery, List.of("title^5", "content^2.2", "answerContent^1.8"), 5.0));
        should.add(crossFieldClause(normalizedQuery, List.of("title^4", "content^1.8", "answerContent^1.5"), 3.5));
        should.add(phraseClause("title", normalizedQuery, 1, 4.5));
        should.add(phraseClause("content", normalizedQuery, 2, 2.2));
        should.add(prefixClause("title", normalizedQuery, 2.0));
        should.add(prefixClause("content", normalizedQuery, 1.2));

        List<String> terms = extractSemanticTerms(normalizedQuery);
        for (String term : terms) {
            should.add(matchClause("title", term, 1.8));
            should.add(matchClause("content", term, 1.3));
            should.add(matchClause("answerContent", term, 1.2));
            if (isAsciiTerm(term) && term.length() >= 4) {
                should.add(fuzzyClause("title", term, 1.15));
                should.add(fuzzyClause("content", term, 1.05));
            }
        }

        for (String variant : expandSemanticVariants(normalizedQuery, terms)) {
            if (!StringUtils.hasText(variant) || normalizedQuery.equals(variant)) {
                continue;
            }
            should.add(multiMatchClause(variant, List.of("title^4.5", "content^2", "answerContent^1.7"), 2.0));
            should.add(phraseClause("title", variant, 2, 1.4));
        }

        Map<String, Object> bool = new HashMap<>();
        bool.put("must", must);
        bool.put("should", should);
        bool.put("minimum_should_match", 0);
        return Map.of("bool", bool);
    }

    private Map<String, Object> kbSemanticClause(String query) {
        String normalizedQuery = normalizeQuery(query);
        if (!StringUtils.hasText(normalizedQuery)) {
            normalizedQuery = query.trim();
        }
        List<Object> must = new ArrayList<>();
        must.add(coreQueryClause(
            normalizedQuery,
            List.of("title^5.5", "summary^3.5", "content^2.2")
        ));
        List<Object> should = new ArrayList<>();
        should.add(multiMatchClause(normalizedQuery, List.of("title^4.5", "summary^3.2", "content^2.1"), 4.2));
        should.add(crossFieldClause(normalizedQuery, List.of("title^4", "summary^2.8", "content^1.8"), 2.6));
        should.add(phraseClause("title", normalizedQuery, 1, 3.8));
        should.add(phraseClause("summary", normalizedQuery, 2, 2.4));
        should.add(prefixClause("title", normalizedQuery, 1.9));

        List<String> terms = extractSemanticTerms(normalizedQuery);
        for (String term : terms) {
            should.add(matchClause("title", term, 1.7));
            should.add(matchClause("summary", term, 1.5));
            should.add(matchClause("content", term, 1.2));
            if (isAsciiTerm(term) && term.length() >= 4) {
                should.add(fuzzyClause("title", term, 1.12));
                should.add(fuzzyClause("summary", term, 1.05));
            }
        }

        for (String variant : expandSemanticVariants(normalizedQuery, terms)) {
            if (!StringUtils.hasText(variant) || normalizedQuery.equals(variant)) {
                continue;
            }
            should.add(multiMatchClause(variant, List.of("title^4.2", "summary^3", "content^2"), 1.8));
            should.add(phraseClause("title", variant, 2, 1.3));
        }

        Map<String, Object> bool = new HashMap<>();
        bool.put("must", must);
        bool.put("should", should);
        bool.put("minimum_should_match", 0);
        return Map.of("bool", bool);
    }

    private Map<String, Object> questionHybridQuery(Map<String, Object> baseQuery) {
        List<Map<String, Object>> functions = new ArrayList<>();
        functions.add(Map.of("field_value_factor", Map.of(
            "field", "hotScore",
            "modifier", "log1p",
            "factor", 0.3,
            "missing", 0
        )));
        functions.add(Map.of("field_value_factor", Map.of(
            "field", "answerCount",
            "modifier", "log1p",
            "factor", 0.1,
            "missing", 0
        )));
        functions.add(Map.of("gauss", Map.of(
            "createdAt", Map.of(
                "origin", "now",
                "scale", "90d",
                "offset", "7d",
                "decay", 0.6
            )
        )));
        return Map.of("function_score", Map.of(
            "query", baseQuery,
            "functions", functions,
            "score_mode", "sum",
            "boost_mode", "sum"
        ));
    }

    private Map<String, Object> kbHybridQuery(Map<String, Object> baseQuery) {
        List<Map<String, Object>> functions = new ArrayList<>();
        functions.add(Map.of("field_value_factor", Map.of(
            "field", "hotScore",
            "modifier", "log1p",
            "factor", 0.35,
            "missing", 0
        )));
        functions.add(Map.of("gauss", Map.of(
            "createdAt", Map.of(
                "origin", "now",
                "scale", "120d",
                "offset", "14d",
                "decay", 0.65
            )
        )));
        return Map.of("function_score", Map.of(
            "query", baseQuery,
            "functions", functions,
            "score_mode", "sum",
            "boost_mode", "sum"
        ));
    }

    private List<Object> questionSort(String sortBy, boolean hybrid) {
        return switch (sortBy) {
            case "latest" -> List.of(
                Map.of("createdAt", "desc"),
                Map.of("id", "desc")
            );
            case "hot" -> List.of(
                Map.of("hotScore", "desc"),
                Map.of("createdAt", "desc"),
                Map.of("id", "desc")
            );
            default -> List.of(
                Map.of("_score", "desc"),
                Map.of(hybrid ? "hotScore" : "createdAt", "desc"),
                Map.of("id", "desc")
            );
        };
    }

    private String resolveSearchStrategy() {
        String strategy = properties.getSearchStrategy();
        if (!StringUtils.hasText(strategy)) {
            return "es_lexical";
        }
        return strategy.trim().toLowerCase();
    }

    private Map<String, Object> multiMatchClause(String query, List<String> fields, double boost) {
        Map<String, Object> mm = new HashMap<>();
        mm.put("query", query);
        mm.put("fields", fields);
        mm.put("type", "best_fields");
        mm.put("minimum_should_match", minimumShouldMatch(query));
        mm.put("boost", boost);
        return Map.of("multi_match", mm);
    }

    private Map<String, Object> coreQueryClause(String query, List<String> fields) {
        Map<String, Object> mm = new HashMap<>();
        mm.put("query", query);
        mm.put("fields", fields);
        mm.put("type", "best_fields");
        mm.put("minimum_should_match", coreMinimumShouldMatch(query));
        return Map.of("multi_match", mm);
    }

    private Map<String, Object> crossFieldClause(String query, List<String> fields, double boost) {
        Map<String, Object> mm = new HashMap<>();
        mm.put("query", query);
        mm.put("fields", fields);
        mm.put("type", "cross_fields");
        mm.put("operator", "and");
        mm.put("boost", boost);
        return Map.of("multi_match", mm);
    }

    private Map<String, Object> phraseClause(String field, String query, int slop, double boost) {
        return Map.of("match_phrase", Map.of(
            field, Map.of(
                "query", query,
                "slop", slop,
                "boost", boost
            )
        ));
    }

    private Map<String, Object> prefixClause(String field, String query, double boost) {
        return Map.of("match_bool_prefix", Map.of(
            field, Map.of(
                "query", query,
                "boost", boost
            )
        ));
    }

    private Map<String, Object> matchClause(String field, String query, double boost) {
        return Map.of("match", Map.of(
            field, Map.of(
                "query", query,
                "boost", boost
            )
        ));
    }

    private Map<String, Object> fuzzyClause(String field, String query, double boost) {
        return Map.of("match", Map.of(
            field, Map.of(
                "query", query,
                "fuzziness", "AUTO",
                "prefix_length", 1,
                "boost", boost
            )
        ));
    }

    private String minimumShouldMatch(String query) {
        int tokens = semanticTokenCount(query);
        if (tokens <= 2) {
            return "75%";
        }
        if (tokens <= 4) {
            return "70%";
        }
        return "70%";
    }

    private String coreMinimumShouldMatch(String query) {
        int tokens = semanticTokenCount(query);
        if (tokens <= 2) {
            return "100%";
        }
        if (tokens <= 4) {
            return "75%";
        }
        if (tokens <= 8) {
            return "65%";
        }
        return "55%";
    }

    private int semanticTokenCount(String query) {
        if (!StringUtils.hasText(query)) {
            return 1;
        }
        String normalized = normalizeQuery(query);
        if (!StringUtils.hasText(normalized)) {
            return 1;
        }
        String[] parts = normalized.split("\\s+");
        int wsCount = 0;
        for (String part : parts) {
            if (StringUtils.hasText(part)) {
                wsCount++;
            }
        }
        if (wsCount > 1) {
            return wsCount;
        }
        int chineseCount = 0;
        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            if (c >= 0x4E00 && c <= 0x9FFF) {
                chineseCount++;
            }
        }
        if (chineseCount > 0) {
            return chineseCount;
        }
        return 1;
    }

    private boolean isAsciiTerm(String term) {
        if (!StringUtils.hasText(term)) {
            return false;
        }
        return ASCII_ALNUM.matcher(term).matches();
    }

    private int resolveSemanticTermLimit() {
        int configured = properties.getSemanticTermLimit() == null ? SEMANTIC_TERM_LIMIT : properties.getSemanticTermLimit();
        configured = Math.max(configured, MIN_SEMANTIC_LIMIT);
        return Math.min(configured, MAX_SEMANTIC_TERM_LIMIT);
    }

    private int resolveSemanticVariantLimit() {
        int configured = properties.getSemanticVariantLimit() == null ? SEMANTIC_VARIANT_LIMIT : properties.getSemanticVariantLimit();
        configured = Math.max(configured, MIN_SEMANTIC_LIMIT);
        return Math.min(configured, MAX_SEMANTIC_VARIANT_LIMIT);
    }

    private List<String> extractSemanticTerms(String rawQuery) {
        if (!StringUtils.hasText(rawQuery)) {
            return List.of();
        }
        String normalized = normalizeQuery(rawQuery);
        if (!StringUtils.hasText(normalized)) {
            return List.of();
        }
        int termLimit = resolveSemanticTermLimit();
        LinkedHashSet<String> terms = new LinkedHashSet<>();
        String[] parts = normalized.split("\\s+");
        for (String part : parts) {
            if (!StringUtils.hasText(part)) {
                continue;
            }
            terms.add(part);
            if (containsChinese(part) && part.length() > 2) {
                for (int i = 0; i < part.length() - 1; i++) {
                    String bg = part.substring(i, i + 2);
                    if (StringUtils.hasText(bg)) {
                        terms.add(bg);
                    }
                    if (terms.size() >= termLimit) {
                        break;
                    }
                }
            }
            if (terms.size() >= termLimit) {
                break;
            }
        }
        List<String> result = new ArrayList<>(terms);
        return result.subList(0, Math.min(termLimit, result.size()));
    }

    private List<String> expandSemanticVariants(String query, List<String> terms) {
        int variantLimit = resolveSemanticVariantLimit();
        LinkedHashSet<String> variants = new LinkedHashSet<>();
        String normalizedQuery = normalizeQuery(query);
        variants.add(normalizedQuery);
        List<String> termList = terms == null ? List.of() : terms;
        Set<String> lookupTerms = new LinkedHashSet<>();
        for (String term : termList) {
            if (StringUtils.hasText(term)) {
                lookupTerms.add(term.toLowerCase(Locale.ROOT));
            }
        }
        String lowerQuery = normalizedQuery.toLowerCase(Locale.ROOT);

        for (Map.Entry<String, List<String>> entry : semanticSynonyms().entrySet()) {
            String key = entry.getKey();
            if (!StringUtils.hasText(key)) {
                continue;
            }
            boolean hit = lowerQuery.contains(key.toLowerCase(Locale.ROOT)) || lookupTerms.contains(key.toLowerCase(Locale.ROOT));
            if (!hit) {
                continue;
            }
            for (String syn : entry.getValue()) {
                if (!StringUtils.hasText(syn)) {
                    continue;
                }
                variants.add(normalizeQuery(replaceIgnoreCase(normalizedQuery, key, syn)));
                variants.add(normalizeQuery(normalizedQuery + " " + syn));
                if (variants.size() >= variantLimit) {
                    return new ArrayList<>(variants);
                }
            }
        }
        return new ArrayList<>(variants);
    }

    private Map<String, List<String>> semanticSynonyms() {
        Map<String, LinkedHashSet<String>> merged = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : MEDICAL_SYNONYMS.entrySet()) {
            List<String> group = new ArrayList<>();
            group.add(entry.getKey());
            if (entry.getValue() != null) {
                group.addAll(entry.getValue());
            }
            mergeSynonymGroup(merged, group);
        }
        mergeCustomSynonyms(merged, properties.getSemanticSynonyms());

        Map<String, List<String>> result = new LinkedHashMap<>();
        for (Map.Entry<String, LinkedHashSet<String>> entry : merged.entrySet()) {
            result.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return result;
    }

    private void mergeCustomSynonyms(Map<String, LinkedHashSet<String>> merged, String configured) {
        if (!StringUtils.hasText(configured)) {
            return;
        }
        String normalizedConfigured = configured
            .replace('；', ';')
            .replace('，', ',')
            .replace('、', ',')
            .replace('\t', ' ');
        String[] groups = normalizedConfigured.split("(?:;|\\r\\n|\\n|\\r|\\s{2,})+");
        for (String group : groups) {
            if (!StringUtils.hasText(group)) {
                continue;
            }
            String[] parts = group.split("[,|]+");
            List<String> terms = new ArrayList<>();
            for (String part : parts) {
                String term = normalizeQuery(part);
                if (StringUtils.hasText(term)) {
                    terms.add(term);
                }
            }
            mergeSynonymGroup(merged, terms);
        }
    }

    private void mergeSynonymGroup(Map<String, LinkedHashSet<String>> merged, List<String> group) {
        if (group == null || group.size() < 2) {
            return;
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String term : group) {
            String normalizedTerm = normalizeQuery(term);
            if (StringUtils.hasText(normalizedTerm)) {
                normalized.add(normalizedTerm);
            }
        }
        if (normalized.size() < 2) {
            return;
        }
        for (String term : normalized) {
            LinkedHashSet<String> alternatives = merged.computeIfAbsent(term, ignored -> new LinkedHashSet<>());
            for (String candidate : normalized) {
                if (!term.equalsIgnoreCase(candidate)) {
                    alternatives.add(candidate);
                }
            }
        }
    }

    private String replaceIgnoreCase(String source, String target, String replacement) {
        if (!StringUtils.hasText(source) || !StringUtils.hasText(target) || replacement == null) {
            return source;
        }
        Pattern pattern = Pattern.compile(Pattern.quote(target), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        Matcher matcher = pattern.matcher(source);
        return matcher.replaceAll(Matcher.quoteReplacement(replacement));
    }

    private String normalizeQuery(String query) {
        if (!StringUtils.hasText(query)) {
            return "";
        }
        return query.trim()
            .replaceAll("[\\p{Punct}\\p{IsPunctuation}]+", " ")
            .replaceAll("\\s+", " ");
    }

    private boolean containsChinese(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= 0x4E00 && c <= 0x9FFF) {
                return true;
            }
        }
        return false;
    }

    private List<Long> executeSearch(String index, Map<String, Object> body) {
        String url = buildUrl("/" + index + "/_search");
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            Map<?, ?> response = restTemplate().postForObject(url, entity, Map.class);
            return extractIds(response);
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<Long> extractIds(Map<?, ?> response) {
        if (response == null) {
            return List.of();
        }
        Object hitsObj = response.get("hits");
        if (!(hitsObj instanceof Map<?, ?> hits)) {
            return List.of();
        }
        Object hitListObj = hits.get("hits");
        if (!(hitListObj instanceof List<?> hitList)) {
            return List.of();
        }
        List<Long> ids = new ArrayList<>();
        for (Object item : hitList) {
            if (!(item instanceof Map<?, ?> hit)) {
                continue;
            }
            Object sourceObj = hit.get("_source");
            if (sourceObj instanceof Map<?, ?> source) {
                Object idObj = source.get("id");
                Long id = parseLong(idObj);
                if (id != null) {
                    ids.add(id);
                    continue;
                }
            }
            Long id = parseLong(hit.get("_id"));
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception ignored) {
            return null;
        }
    }

    private IndexRoute getQuestionRoute() {
        if (questionRoute == null) {
            questionRoute = ensureIndexRoute(properties.getIndexQuestion(), questionMapping());
        }
        return questionRoute;
    }

    private IndexRoute getKbRoute() {
        if (kbRoute == null) {
            kbRoute = ensureIndexRoute(properties.getIndexKb(), kbMapping());
        }
        return kbRoute;
    }

    private IndexRoute ensureIndexRoute(String logicalName, Map<String, Object> mapping) {
        if (!StringUtils.hasText(logicalName)) {
            return null;
        }
        String readAlias = logicalName + READ_ALIAS_SUFFIX;
        String writeAlias = logicalName + WRITE_ALIAS_SUFFIX;
        String initialIndex = logicalName + INITIAL_VERSION_SUFFIX;

        boolean hasReadAlias = aliasExists(readAlias);
        boolean hasWriteAlias = aliasExists(writeAlias);
        if (hasReadAlias && hasWriteAlias) {
            return new IndexRoute(readAlias, writeAlias);
        }

        if (indexExists(logicalName)) {
            ensureAliasesForExistingIndex(logicalName, readAlias, writeAlias);
            return new IndexRoute(readAlias, writeAlias);
        }

        if (!indexExists(initialIndex)) {
            createIndexWithAliases(initialIndex, readAlias, writeAlias, mapping);
        } else {
            ensureAliasesForExistingIndex(initialIndex, readAlias, writeAlias);
        }
        return new IndexRoute(readAlias, writeAlias);
    }

    private boolean indexExists(String index) {
        try {
            restTemplate().exchange(buildUrl("/" + index), HttpMethod.GET, null, String.class);
            return true;
        } catch (HttpClientErrorException.NotFound ignored) {
            return false;
        }
    }

    private boolean aliasExists(String alias) {
        try {
            restTemplate().exchange(buildUrl("/_alias/" + alias), HttpMethod.GET, null, String.class);
            return true;
        } catch (HttpClientErrorException.NotFound ignored) {
            return false;
        }
    }

    private void createIndexWithAliases(String indexName, String readAlias, String writeAlias, Map<String, Object> mapping) {
        Map<String, Object> aliases = new HashMap<>();
        aliases.put(readAlias, Collections.emptyMap());
        aliases.put(writeAlias, Map.of("is_write_index", true));

        Map<String, Object> payload = new HashMap<>(mapping);
        payload.put("aliases", aliases);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        restTemplate().exchange(buildUrl("/" + indexName), HttpMethod.PUT, entity, String.class);
    }

    private void ensureAliasesForExistingIndex(String index, String readAlias, String writeAlias) {
        List<Map<String, Object>> actions = new ArrayList<>();
        if (!aliasExists(readAlias)) {
            actions.add(Map.of("add", Map.of("index", index, "alias", readAlias)));
        }
        if (!aliasExists(writeAlias)) {
            actions.add(Map.of("add", Map.of("index", index, "alias", writeAlias, "is_write_index", true)));
        }
        if (actions.isEmpty()) {
            return;
        }
        Map<String, Object> body = Map.of("actions", actions);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate().postForObject(buildUrl("/_aliases"), entity, String.class);
    }

    private void bulkIndex(String index, String body) {
        if (!StringUtils.hasText(body)) {
            return;
        }
        String url = buildUrl("/" + index + "/_bulk?refresh=true");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/x-ndjson"));
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        restTemplate().postForObject(url, entity, String.class);
    }

    private String buildQuestionBulk(List<SearchQuestionDoc> docs) {
        StringBuilder sb = new StringBuilder();
        for (SearchQuestionDoc doc : docs) {
            if (doc == null || doc.getId() == null) {
                continue;
            }
            Map<String, Object> source = new HashMap<>();
            source.put("id", doc.getId());
            source.put("title", safe(doc.getTitle()));
            source.put("content", safe(doc.getContent()));
            source.put("answerContent", safe(doc.getAnswerContent()));
            source.put("categoryId", doc.getCategoryId());
            source.put("topicId", doc.getTopicId());
            source.put("answerCount", doc.getAnswerCount() == null ? 0 : doc.getAnswerCount());
            source.put("viewCount", doc.getViewCount() == null ? 0 : doc.getViewCount());
            source.put("likeCount", doc.getLikeCount() == null ? 0 : doc.getLikeCount());
            source.put("favoriteCount", doc.getFavoriteCount() == null ? 0 : doc.getFavoriteCount());
            source.put("hotScore", questionHotScore(doc));
            source.put("status", doc.getStatus() == null ? 1 : doc.getStatus());
            source.put("createdAt", formatTime(doc.getCreatedAt()));

            sb.append("{\"index\":{\"_id\":\"").append(doc.getId()).append("\"}}").append("\n");
            sb.append(toJson(source)).append("\n");
        }
        return sb.toString();
    }

    private String buildKbBulk(List<SearchKbDoc> docs) {
        StringBuilder sb = new StringBuilder();
        for (SearchKbDoc doc : docs) {
            if (doc == null || doc.getId() == null) {
                continue;
            }
            Map<String, Object> source = new HashMap<>();
            source.put("id", doc.getId());
            source.put("title", safe(doc.getTitle()));
            source.put("summary", safe(doc.getSummary()));
            source.put("content", safe(doc.getContent()));
            source.put("source", safe(doc.getSource()));
            source.put("viewCount", doc.getViewCount() == null ? 0 : doc.getViewCount());
            source.put("likeCount", doc.getLikeCount() == null ? 0 : doc.getLikeCount());
            source.put("favoriteCount", doc.getFavoriteCount() == null ? 0 : doc.getFavoriteCount());
            source.put("hotScore", kbHotScore(doc));
            source.put("status", doc.getStatus() == null ? 1 : doc.getStatus());
            source.put("createdAt", formatTime(doc.getCreatedAt()));

            sb.append("{\"index\":{\"_id\":\"").append(doc.getId()).append("\"}}").append("\n");
            sb.append(toJson(source)).append("\n");
        }
        return sb.toString();
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private double questionHotScore(SearchQuestionDoc doc) {
        int like = doc.getLikeCount() == null ? 0 : doc.getLikeCount();
        int answer = doc.getAnswerCount() == null ? 0 : doc.getAnswerCount();
        int view = doc.getViewCount() == null ? 0 : doc.getViewCount();
        int favorite = doc.getFavoriteCount() == null ? 0 : doc.getFavoriteCount();
        return like * 3.0 + answer * 2.0 + favorite * 2.0 + view * 0.2;
    }

    private double kbHotScore(SearchKbDoc doc) {
        int like = doc.getLikeCount() == null ? 0 : doc.getLikeCount();
        int view = doc.getViewCount() == null ? 0 : doc.getViewCount();
        int favorite = doc.getFavoriteCount() == null ? 0 : doc.getFavoriteCount();
        return like * 3.0 + favorite * 2.0 + view * 0.2;
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? null : ES_TIME.format(time);
    }

    private Map<String, Object> questionMapping() {
        Map<String, Object> props = new HashMap<>();
        props.put("id", Map.of("type", "long"));
        props.put("title", Map.of("type", "text"));
        props.put("content", Map.of("type", "text"));
        props.put("answerContent", Map.of("type", "text"));
        props.put("categoryId", Map.of("type", "long"));
        props.put("topicId", Map.of("type", "long"));
        props.put("answerCount", Map.of("type", "integer"));
        props.put("viewCount", Map.of("type", "integer"));
        props.put("likeCount", Map.of("type", "integer"));
        props.put("favoriteCount", Map.of("type", "integer"));
        props.put("hotScore", Map.of("type", "double"));
        props.put("status", Map.of("type", "integer"));
        props.put("createdAt", Map.of("type", "date"));
        return Map.of("mappings", Map.of("properties", props));
    }

    private Map<String, Object> kbMapping() {
        Map<String, Object> props = new HashMap<>();
        props.put("id", Map.of("type", "long"));
        props.put("title", Map.of("type", "text"));
        props.put("summary", Map.of("type", "text"));
        props.put("content", Map.of("type", "text"));
        props.put("source", Map.of("type", "keyword"));
        props.put("viewCount", Map.of("type", "integer"));
        props.put("likeCount", Map.of("type", "integer"));
        props.put("favoriteCount", Map.of("type", "integer"));
        props.put("hotScore", Map.of("type", "double"));
        props.put("status", Map.of("type", "integer"));
        props.put("createdAt", Map.of("type", "date"));
        return Map.of("mappings", Map.of("properties", props));
    }

    private RestTemplate restTemplate() {
        if (restTemplate == null) {
            restTemplate = restTemplateBuilder.build();
        }
        return restTemplate;
    }

    private String buildUrl(String path) {
        String base = properties.getBaseUrl();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + path;
    }

    private record IndexRoute(String readAlias, String writeAlias) {
    }
}
