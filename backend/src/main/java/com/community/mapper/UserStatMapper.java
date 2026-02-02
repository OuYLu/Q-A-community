package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.UserStat;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserStatMapper extends BaseMapper<UserStat> {
}