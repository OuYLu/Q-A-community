package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.UserBrowseHistory;
import com.community.vo.AppMyHistoryItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserBrowseHistoryMapper extends BaseMapper<UserBrowseHistory> {
    List<AppMyHistoryItemVO> selectMyHistory(@Param("userId") Long userId);
}
