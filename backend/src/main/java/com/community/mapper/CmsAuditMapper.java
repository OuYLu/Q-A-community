package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.CmsAudit;
import com.community.vo.CmsAuditDetailRowVO;
import com.community.vo.CmsAuditPageItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CmsAuditMapper extends BaseMapper<CmsAudit> {
    List<CmsAuditPageItemVO> selectAdminAuditPage(@Param("bizType") Integer bizType,
                                                  @Param("auditStatus") Integer auditStatus,
                                                  @Param("triggerSource") Integer triggerSource,
                                                  @Param("keyword") String keyword,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime,
                                                  @Param("sortBy") String sortBy,
                                                  @Param("sortOrder") String sortOrder);

    CmsAuditDetailRowVO selectAdminAuditDetail(@Param("id") Long id);
}
