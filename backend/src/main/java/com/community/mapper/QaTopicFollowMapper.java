package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.QaTopicFollow;
import com.community.vo.AppFollowTopicItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QaTopicFollowMapper extends BaseMapper<QaTopicFollow> {
    List<AppFollowTopicItemVO> selectMyFollowedTopics(@Param("userId") Long userId);
}
