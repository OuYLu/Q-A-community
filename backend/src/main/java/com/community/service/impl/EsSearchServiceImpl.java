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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
        Map.entry("高血压", List.of("血压高", "hypertension")),
        Map.entry("糖尿病", List.of("血糖高", "diabetes")),
        Map.entry("脑梗", List.of("脑梗塞", "脑梗死")),
        Map.entry("中风", List.of("卒中")),
        Map.entry("发烧", List.of("发热")),
        Map.entry("感冒", List.of("上呼吸道感染")),
        Map.entry("头晕", List.of("眩晕")),
        Map.entry("胃疼", List.of("胃痛")),
        Map.entry("失眠", List.of("睡不着", "睡眠障碍")),
        Map.entry("便秘", List.of("排便困难"))
    );

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
            "fields", List.of("title^4", "content")
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
        List<Object> should = new ArrayList<>();
        should.add(Map.of("multi_match", Map.of(
            "query", query,
            "fields", List.of("title^5", "content^2"),
            "type", "best_fields"
        )));
        should.add(Map.of("match_phrase", Map.of(
            "title", Map.of("query", query, "slop", 2, "boost", 4)
        )));
        should.add(Map.of("match_bool_prefix", Map.of(
            "title", Map.of("query", query, "boost", 2)
        )));

        List<String> terms = extractSemanticTerms(query);
        for (String variant : expandSemanticVariants(query, terms)) {
            if (query.equals(variant)) {
                continue;
            }
            should.add(Map.of("multi_match", Map.of(
                "query", variant,
                "fields", List.of("title^4", "content^2"),
                "type", "best_fields",
                "boost", 1.8
            )));
        }
        for (String term : terms) {
            should.add(Map.of("match", Map.of(
                "title", Map.of("query", term, "boost", 1.5)
            )));
            should.add(Map.of("match", Map.of(
                "content", Map.of("query", term, "boost", 1.1)
            )));
        }

        Map<String, Object> bool = new HashMap<>();
        bool.put("should", should);
        bool.put("minimum_should_match", 1);
        return Map.of("bool", bool);
    }

    private Map<String, Object> kbSemanticClause(String query) {
        List<Object> should = new ArrayList<>();
        should.add(Map.of("multi_match", Map.of(
            "query", query,
            "fields", List.of("title^4", "summary^3", "content^2"),
            "type", "best_fields"
        )));
        should.add(Map.of("match_phrase", Map.of(
            "title", Map.of("query", query, "slop", 2, "boost", 3.5)
        )));
        should.add(Map.of("match_bool_prefix", Map.of(
            "title", Map.of("query", query, "boost", 2)
        )));

        List<String> terms = extractSemanticTerms(query);
        for (String variant : expandSemanticVariants(query, terms)) {
            if (query.equals(variant)) {
                continue;
            }
            should.add(Map.of("multi_match", Map.of(
                "query", variant,
                "fields", List.of("title^4", "summary^3", "content^2"),
                "type", "best_fields",
                "boost", 1.7
            )));
        }
        for (String term : terms) {
            should.add(Map.of("match", Map.of(
                "title", Map.of("query", term, "boost", 1.5)
            )));
            should.add(Map.of("match", Map.of(
                "summary", Map.of("query", term, "boost", 1.3)
            )));
            should.add(Map.of("match", Map.of(
                "content", Map.of("query", term, "boost", 1.1)
            )));
        }

        Map<String, Object> bool = new HashMap<>();
        bool.put("should", should);
        bool.put("minimum_should_match", 1);
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
        return Map.of("function_score", Map.of(
            "query", baseQuery,
            "functions", functions,
            "score_mode", "sum",
            "boost_mode", "sum"
        ));
    }

    private Map<String, Object> kbHybridQuery(Map<String, Object> baseQuery) {
        return Map.of("function_score", Map.of(
            "query", baseQuery,
            "functions", List.of(Map.of("field_value_factor", Map.of(
                "field", "hotScore",
                "modifier", "log1p",
                "factor", 0.35,
                "missing", 0
            ))),
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

    private List<String> extractSemanticTerms(String rawQuery) {
        if (!StringUtils.hasText(rawQuery)) {
            return List.of();
        }
        String normalized = normalizeQuery(rawQuery);
        if (!StringUtils.hasText(normalized)) {
            return List.of();
        }
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
                    if (terms.size() >= SEMANTIC_TERM_LIMIT) {
                        break;
                    }
                }
            }
            if (terms.size() >= SEMANTIC_TERM_LIMIT) {
                break;
            }
        }
        return new ArrayList<>(terms).subList(0, Math.min(SEMANTIC_TERM_LIMIT, terms.size()));
    }

    private List<String> expandSemanticVariants(String query, List<String> terms) {
        LinkedHashSet<String> variants = new LinkedHashSet<>();
        variants.add(normalizeQuery(query));
        for (Map.Entry<String, List<String>> entry : MEDICAL_SYNONYMS.entrySet()) {
            String key = entry.getKey();
            List<String> syns = entry.getValue();
            if (!StringUtils.hasText(key) || syns == null || syns.isEmpty()) {
                continue;
            }
            String lowerQuery = query.toLowerCase(Locale.ROOT);
            boolean hit = lowerQuery.contains(key.toLowerCase(Locale.ROOT));
            if (!hit && terms != null) {
                for (String t : terms) {
                    if (key.equalsIgnoreCase(t)) {
                        hit = true;
                        break;
                    }
                }
            }
            if (hit) {
                for (String syn : syns) {
                    if (!StringUtils.hasText(syn)) {
                        continue;
                    }
                    variants.add(normalizeQuery(query.replace(key, syn)));
                    variants.add(normalizeQuery(query + " " + syn));
                    if (variants.size() >= SEMANTIC_VARIANT_LIMIT) {
                        return new ArrayList<>(variants);
                    }
                }
            }
        }
        return new ArrayList<>(variants);
    }

    private String normalizeQuery(String query) {
        if (!StringUtils.hasText(query)) {
            return "";
        }
        return query.trim()
            .replaceAll("[\\p{Punct}，。！？；：“”‘’（）【】《》、]+", " ")
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
