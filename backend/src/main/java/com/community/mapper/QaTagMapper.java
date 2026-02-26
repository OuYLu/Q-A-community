package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.QaTag;
import com.community.vo.AppSearchTagVO;
import com.community.vo.TagRecentQuestionVO;
import com.community.vo.TagUsageTrendPointVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QaTagMapper extends BaseMapper<QaTag> {
    List<TagRecentQuestionVO> selectRecentQuestions(@Param("tagId") Long tagId);

    List<TagUsageTrendPointVO> selectUsageTrend(@Param("tagId") Long tagId,
                                                @Param("daysMinusOne") Integer daysMinusOne);

    List<AppSearchTagVO> selectAppSearchTags(@Param("query") String query,
                                             @Param("limit") Integer limit);
}
