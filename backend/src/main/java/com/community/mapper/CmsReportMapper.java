package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.CmsReport;
import com.community.vo.CmsReportDetailRowVO;
import com.community.vo.CmsReportPageItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CmsReportMapper extends BaseMapper<CmsReport> {
    List<CmsReportPageItemVO> selectAdminReportPage(@Param("bizType") Integer bizType,
                                                    @Param("status") Integer status,
                                                    @Param("reasonType") Integer reasonType,
                                                    @Param("keyword") String keyword,
                                                    @Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime);

    CmsReportDetailRowVO selectAdminReportDetail(@Param("id") Long id);
}
