package com.community.mapper;

import com.community.vo.DashboardContentTrendVO;
import com.community.vo.DashboardGovernanceTrendVO;
import com.community.vo.DashboardHotTagVO;
import com.community.vo.DashboardHotTopicVO;
import com.community.vo.DashboardNewTagVO;
import com.community.vo.DashboardNewTagTrendPointVO;
import com.community.vo.DashboardUserActivityTrendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DashboardStatMapper {
    long countTodayQuestion();

    long countTodayAnswer();

    long countTodayComment();

    long countTodayReport();

    long countPendingAudit();

    long countPendingReport();

    List<DashboardContentTrendVO> selectContentTrend(@Param("daysMinusOne") Integer daysMinusOne);

    List<DashboardGovernanceTrendVO> selectGovernanceTrend(@Param("daysMinusOne") Integer daysMinusOne);

    List<DashboardUserActivityTrendVO> selectUserActivityTrend(@Param("daysMinusOne") Integer daysMinusOne);

    List<DashboardHotTagVO> selectHotTags(@Param("limit") Integer limit);

    List<DashboardHotTopicVO> selectHotTopics(@Param("limit") Integer limit);

    long countNewUserTags(@Param("daysMinusOne") Integer daysMinusOne);

    List<DashboardNewTagVO> selectNewUserTags(@Param("daysMinusOne") Integer daysMinusOne,
                                              @Param("limit") Integer limit);

    List<DashboardNewTagTrendPointVO> selectNewUserTagTrend(@Param("daysMinusOne") Integer daysMinusOne);
}
