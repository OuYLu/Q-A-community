package com.community.service;

import com.community.dto.CmsReportHandleDTO;
import com.community.dto.CmsReportPageQueryDTO;
import com.community.vo.CmsReportDetailVO;
import com.community.vo.CmsReportPageItemVO;
import com.github.pagehelper.PageInfo;

public interface CmsReportAdminService {
    PageInfo<CmsReportPageItemVO> page(CmsReportPageQueryDTO query);

    CmsReportDetailVO detail(Long id);

    void handle(Long id, CmsReportHandleDTO dto);

    void toAudit(Long id);
}
