package com.community.service;

import com.community.dto.QaTagQueryDTO;
import com.community.dto.QaTagSaveDTO;
import com.community.entity.QaTag;
import com.community.vo.TagDetailExtraVO;
import com.community.vo.TagUsageTrendPointVO;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface QaTagAdminService {
    PageInfo<QaTag> list(QaTagQueryDTO query);

    QaTag create(QaTagSaveDTO dto);

    QaTag update(Long id, QaTagSaveDTO dto);

    void delete(Long id);

    void batchEnable(List<Long> ids);

    void batchDisable(List<Long> ids);

    QaTag getById(Long id);

    TagDetailExtraVO getDetailExtra(Long id);

    List<TagUsageTrendPointVO> usageTrend(Long id, Integer days);
}
