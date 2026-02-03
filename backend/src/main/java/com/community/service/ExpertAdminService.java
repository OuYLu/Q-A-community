package com.community.service;

import com.community.dto.ExpertReviewDTO;
import com.community.entity.ExpertApply;
import com.github.pagehelper.PageInfo;

public interface ExpertAdminService {
    PageInfo<ExpertApply> listApplies(Integer status, int pageNum, int pageSize);

    void review(ExpertReviewDTO dto);
}
