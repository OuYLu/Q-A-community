package com.community.service;

import com.community.dto.CmsSensitiveWordQueryDTO;
import com.community.dto.CmsSensitiveWordSaveDTO;
import com.community.entity.CmsSensitiveWord;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface CmsSensitiveWordAdminService {
    PageInfo<CmsSensitiveWord> list(CmsSensitiveWordQueryDTO query);

    CmsSensitiveWord create(CmsSensitiveWordSaveDTO dto);

    CmsSensitiveWord update(Long id, CmsSensitiveWordSaveDTO dto);

    CmsSensitiveWord getById(Long id);

    void batchEnable(List<Long> ids);

    void batchDisable(List<Long> ids);
}

