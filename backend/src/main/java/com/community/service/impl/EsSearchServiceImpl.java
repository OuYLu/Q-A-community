package com.community.service.impl;

import com.community.config.EsProperties;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EsSearchServiceImpl implements EsSearchService {
    private static final DateTimeFormatter ES_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final EsProperties properties;
    private final RestTemplateBuilder restTemplateBuilder;
    private final ObjectMapper objectMapper;
    private final QaQuestionMapper qaQuestionMapper;
    private final KbEntryMapper kbEntryMapper;

    private RestTemplate restTemplate;

    @Override
    public boolean isEnabled() {
        return properties != null && properties.isEnabled() && StringUtils.hasText(properties.getBaseUrl());
    }

    @Override
    public List<Long> searchQuestionIds(String query, int from, int size, Long categoryId, Long topicId, Boolean onlyUnsolved) {
        if (!isEnabled() || !StringUtils.hasText(query)) {
            return List.of();
        }
        Map<String, Object> body = buildQuestionSearchBody(query, from, size, categoryId, topicId, onlyUnsolved);
        return executeSearch(properties.getIndexQuestion(), body);
    }

    @Override
    public List<Long> searchKbIds(String query, int from, int size) {
        if (!isEnabled() || !StringUtils.hasText(query)) {
            return List.of();
        }
        Map<String, Object> body = buildKbSearchBody(query, from, size);
        return executeSearch(properties.getIndexKb(), body);
    }

    @Override
    public void reindexAll() {
        if (!isEnabled()) {
            return;
        }
        ensureIndex(properties.getIndexQuestion(), questionMapping());
        ensureIndex(properties.getIndexKb(), kbMapping());

        List<SearchQuestionDoc> questions = qaQuestionMapper.selectSearchQuestionDocs();
        if (!CollectionUtils.isEmpty(questions)) {
            bulkIndex(properties.getIndexQuestion(), buildQuestionBulk(questions));
        }

        List<SearchKbDoc> kbs = kbEntryMapper.selectSearchKbDocs();
        if (!CollectionUtils.isEmpty(kbs)) {
            bulkIndex(properties.getIndexKb(), buildKbBulk(kbs));
        }
    }

    @Override
    public void indexQuestion(SearchQuestionDoc doc) {
        if (!isEnabled() || doc == null || doc.getId() == null) {
            return;
        }
        ensureIndex(properties.getIndexQuestion(), questionMapping());
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", doc.getId());
        payload.put("title", safe(doc.getTitle()));
        payload.put("content", safe(doc.getContent()));
        payload.put("categoryId", doc.getCategoryId());
        payload.put("topicId", doc.getTopicId());
        payload.put("answerCount", doc.getAnswerCount() == null ? 0 : doc.getAnswerCount());
        payload.put("status", doc.getStatus() == null ? 1 : doc.getStatus());
        payload.put("createdAt", formatTime(doc.getCreatedAt()));
        indexDoc(properties.getIndexQuestion(), doc.getId(), payload);
    }

    @Override
    public void indexKb(SearchKbDoc doc) {
        if (!isEnabled() || doc == null || doc.getId() == null) {
            return;
        }
        ensureIndex(properties.getIndexKb(), kbMapping());
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", doc.getId());
        payload.put("title", safe(doc.getTitle()));
        payload.put("summary", safe(doc.getSummary()));
        payload.put("content", safe(doc.getContent()));
        payload.put("source", safe(doc.getSource()));
        payload.put("createdAt", formatTime(doc.getCreatedAt()));
        indexDoc(properties.getIndexKb(), doc.getId(), payload);
    }

    private void indexDoc(String index, Long id, Map<String, Object> payload) {
        String url = buildUrl("/" + index + "/_doc/" + id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        restTemplate().exchange(url, HttpMethod.PUT, entity, String.class);
    }

    private Map<String, Object> buildQuestionSearchBody(String query, int from, int size,
                                                        Long categoryId, Long topicId, Boolean onlyUnsolved) {
        List<Object> must = new ArrayList<>();
        must.add(Map.of("multi_match", Map.of(
            "query", query,
            "fields", List.of("title^3", "content")
        )));

        List<Object> filters = new ArrayList<>();
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
        if (!filters.isEmpty()) {
            bool.put("filter", filters);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("from", Math.max(from, 0));
        body.put("size", Math.max(size, 1));
        body.put("query", Map.of("bool", bool));
        body.put("sort", List.of(
            Map.of("_score", "desc"),
            Map.of("createdAt", "desc"),
            Map.of("id", "desc")
        ));
        return body;
    }

    private Map<String, Object> buildKbSearchBody(String query, int from, int size) {
        Map<String, Object> body = new HashMap<>();
        body.put("from", Math.max(from, 0));
        body.put("size", Math.max(size, 1));
        body.put("query", Map.of("multi_match", Map.of(
            "query", query,
            "fields", List.of("title^3", "summary^2", "content")
        )));
        body.put("sort", List.of(
            Map.of("_score", "desc"),
            Map.of("createdAt", "desc"),
            Map.of("id", "desc")
        ));
        return body;
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

    private void ensureIndex(String index, Map<String, Object> mapping) {
        if (!StringUtils.hasText(index)) {
            return;
        }
        String url = buildUrl("/" + index);
        try {
            restTemplate().exchange(url, HttpMethod.GET, null, String.class);
        } catch (HttpClientErrorException.NotFound notFound) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(mapping, headers);
            restTemplate().exchange(url, HttpMethod.PUT, entity, String.class);
        }
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
            sb.append("{\"index\":{\"_id\":\"").append(doc.getId()).append("\"}}").append("\n");
            sb.append(toJson(Map.of(
                "id", doc.getId(),
                "title", safe(doc.getTitle()),
                "content", safe(doc.getContent()),
                "categoryId", doc.getCategoryId(),
                "topicId", doc.getTopicId(),
                "answerCount", doc.getAnswerCount() == null ? 0 : doc.getAnswerCount(),
                "status", doc.getStatus() == null ? 1 : doc.getStatus(),
                "createdAt", formatTime(doc.getCreatedAt())
            ))).append("\n");
        }
        return sb.toString();
    }

    private String buildKbBulk(List<SearchKbDoc> docs) {
        StringBuilder sb = new StringBuilder();
        for (SearchKbDoc doc : docs) {
            if (doc == null || doc.getId() == null) {
                continue;
            }
            sb.append("{\"index\":{\"_id\":\"").append(doc.getId()).append("\"}}").append("\n");
            sb.append(toJson(Map.of(
                "id", doc.getId(),
                "title", safe(doc.getTitle()),
                "summary", safe(doc.getSummary()),
                "content", safe(doc.getContent()),
                "source", safe(doc.getSource()),
                "createdAt", formatTime(doc.getCreatedAt())
            ))).append("\n");
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

    private String formatTime(LocalDateTime time) {
        return time == null ? null : ES_TIME.format(time);
    }

    private Map<String, Object> questionMapping() {
        Map<String, Object> props = new HashMap<>();
        props.put("id", Map.of("type", "long"));
        props.put("title", Map.of("type", "text"));
        props.put("content", Map.of("type", "text"));
        props.put("categoryId", Map.of("type", "long"));
        props.put("topicId", Map.of("type", "long"));
        props.put("answerCount", Map.of("type", "integer"));
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
}