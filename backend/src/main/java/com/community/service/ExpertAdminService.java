package com.community.service;

import com.community.dto.ExpertReviewDTO;
import com.community.dto.ExpertApplyQueryDTO;
import com.community.entity.ExpertApply;
import com.community.vo.ExpertApplyDetailVO;
import com.github.pagehelper.PageInfo;

public interface ExpertAdminService {
    PageInfo<ExpertApply> listApplies(ExpertApplyQueryDTO query);

    void review(ExpertReviewDTO dto);

    ExpertApplyDetailVO getDetailByUserId(Long userId);
}
