package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.QaTopicCategory;
import com.community.vo.TopicCategoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QaTopicCategoryMapper extends BaseMapper<QaTopicCategory> {
    List<TopicCategoryVO> selectCategoriesByTopicId(@Param("topicId") Long topicId);
}
