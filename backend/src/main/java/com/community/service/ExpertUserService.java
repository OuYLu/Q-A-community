package com.community.service;

import com.community.dto.ExpertStatusUpdateDTO;
import com.community.dto.ExpertUserQueryDTO;
import com.community.vo.ExpertManageVO;
import com.github.pagehelper.PageInfo;

public interface ExpertUserService {
    PageInfo<ExpertManageVO> listExperts(ExpertUserQueryDTO query);

    void updateExpertStatus(Long userId, ExpertStatusUpdateDTO dto);

    ExpertManageVO getExpertDetail(Long userId);
}
