package com.community.service;

import com.community.dto.KbCategorySaveDTO;
import com.community.dto.KbCategoryStatusDTO;
import com.community.dto.KbEntryPageQueryDTO;
import com.community.dto.KbEntrySaveDTO;
import com.community.dto.KbEntryStatusDTO;
import com.community.vo.KbCategoryTreeVO;
import com.community.vo.KbEntryDetailVO;
import com.community.vo.KbEntryPageItemVO;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface KbAdminService {
    List<KbCategoryTreeVO> categoryTree();

    Long createCategory(KbCategorySaveDTO dto);

    void updateCategory(Long id, KbCategorySaveDTO dto);

    void updateCategoryStatus(Long id, KbCategoryStatusDTO dto);

    PageInfo<KbEntryPageItemVO> entryPage(KbEntryPageQueryDTO query);

    KbEntryDetailVO entryDetail(Long id);

    Long createEntry(KbEntrySaveDTO dto);

    void updateEntry(Long id, KbEntrySaveDTO dto);

    void updateEntryStatus(Long id, KbEntryStatusDTO dto);
}
