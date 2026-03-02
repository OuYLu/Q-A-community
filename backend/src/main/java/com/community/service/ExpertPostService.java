package com.community.service;

import com.community.dto.AppExpertPostCreateDTO;
import com.community.dto.AppExpertPostPageQueryDTO;
import com.community.vo.AppKbCategoryVO;
import com.community.vo.AppExpertPostDetailVO;
import com.community.vo.AppExpertPostItemVO;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface ExpertPostService {
    List<AppKbCategoryVO> categories();

    Long create(AppExpertPostCreateDTO dto);

    void update(Long id, AppExpertPostCreateDTO dto);

    void delete(Long id);

    PageInfo<AppExpertPostItemVO> page(AppExpertPostPageQueryDTO query);

    PageInfo<AppExpertPostItemVO> myPage(AppExpertPostPageQueryDTO query);

    AppExpertPostDetailVO detail(Long id);
}
