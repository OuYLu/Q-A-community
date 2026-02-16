package com.community.service;

import com.community.dto.QaCategoryQueryDTO;
import com.community.dto.QaCategorySaveDTO;
import com.community.entity.QaCategory;
import com.community.vo.CategoryListVO;
import com.community.vo.CategoryTreeVO;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface QaCategoryAdminService {
    PageInfo<CategoryListVO> list(QaCategoryQueryDTO query);

    List<CategoryTreeVO> listTreeLazy(Long parentId);

    QaCategory create(QaCategorySaveDTO dto);

    QaCategory update(Long id, QaCategorySaveDTO dto);

    void delete(Long id);

    QaCategory getById(Long id);
}
