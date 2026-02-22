package com.community.service;

import com.community.dto.CmsAuditBatchReviewDTO;
import com.community.dto.CmsAuditPageQueryDTO;
import com.community.dto.CmsAuditReviewDTO;
import com.community.vo.CmsAuditDetailVO;
import com.community.vo.CmsAuditPageItemVO;
import com.github.pagehelper.PageInfo;

public interface CmsAuditAdminService {
    PageInfo<CmsAuditPageItemVO> page(CmsAuditPageQueryDTO query);

    CmsAuditDetailVO detail(Long id);

    void review(Long id, CmsAuditReviewDTO dto);

    void batchReview(CmsAuditBatchReviewDTO dto);

    void reopen(Long id);
}
