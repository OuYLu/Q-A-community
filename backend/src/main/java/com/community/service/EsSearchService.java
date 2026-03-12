package com.community.service;

import com.community.vo.SearchKbDoc;
import com.community.vo.SearchQuestionDoc;

import java.util.List;

public interface EsSearchService {
    boolean isEnabled();

    void prepareIndices();

    void syncQuestionById(Long questionId);

    void syncKbById(Long kbId);

    List<Long> searchQuestionIds(String query, int from, int size, Long categoryId, Long topicId, Boolean onlyUnsolved, String sortBy);

    List<Long> searchKbIds(String query, int from, int size);

    List<String> buildSemanticTerms(String query, int limit);

    void reindexAll();

    void indexQuestion(SearchQuestionDoc doc);

    void indexKb(SearchKbDoc doc);
}
