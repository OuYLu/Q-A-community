package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.SearchQueryLog;
import com.community.vo.AppSearchHistoryVO;
import com.community.vo.AppSearchHotVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SearchQueryLogMapper extends BaseMapper<SearchQueryLog> {
    List<AppSearchHotVO> selectHotQueries(@Param("limit") Integer limit);

    List<AppSearchHistoryVO> selectHistory(@Param("userId") Long userId,
                                           @Param("limit") Integer limit);

    int deleteByUserId(@Param("userId") Long userId);
}
