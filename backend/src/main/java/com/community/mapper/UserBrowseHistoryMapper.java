package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.UserBrowseHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserBrowseHistoryMapper extends BaseMapper<UserBrowseHistory> {
}