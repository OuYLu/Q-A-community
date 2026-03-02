package com.community.service;

import com.community.vo.SearchKbDoc;
import com.community.vo.SearchQuestionDoc;

import java.util.List;

public interface EsSearchService {
    boolean isEnabled();

    List<Long> searchQuestionIds(String query, int from, int size, Long categoryId, Long topicId, Boolean onlyUnsolved);

    List<Long> searchKbIds(String query, int from, int size);

    void reindexAll();

    void indexQuestion(SearchQuestionDoc doc);

    void indexKb(SearchKbDoc doc);
}