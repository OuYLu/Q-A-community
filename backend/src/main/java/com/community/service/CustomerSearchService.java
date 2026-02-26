package com.community.service;

import com.community.dto.AppSearchLogDTO;
import com.community.dto.AppSearchQueryDTO;
import com.community.vo.AppSearchHistoryVO;
import com.community.vo.AppSearchHotVO;
import com.community.vo.AppSearchResultVO;

import java.util.List;

public interface CustomerSearchService {
    AppSearchResultVO search(AppSearchQueryDTO query);
    List<AppSearchHotVO> hot(Integer limit);
    List<AppSearchHistoryVO> history(Integer limit);
    void clearHistory();
    void logSearch(AppSearchLogDTO dto);
}


