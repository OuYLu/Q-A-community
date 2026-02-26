package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.UserFollow;
import com.community.vo.AppFollowUserItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {
    List<AppFollowUserItemVO> selectMyFollowing(@Param("userId") Long userId);

    List<AppFollowUserItemVO> selectMyFollowers(@Param("userId") Long userId);
}
