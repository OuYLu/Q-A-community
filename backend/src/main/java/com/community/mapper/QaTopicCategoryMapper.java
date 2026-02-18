package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.QaTopicCategory;
import com.community.vo.TopicCategoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QaTopicCategoryMapper extends BaseMapper<QaTopicCategory> {
    @Select("""
        SELECT
            c.id,
            c.name,
            c.parent_id AS parentId
        FROM qa_topic_category tc
        JOIN qa_category c ON c.id = tc.category_id
        WHERE tc.topic_id = #{topicId}
          AND c.delete_flag = 0
        ORDER BY c.sort ASC, c.id ASC
        """)
    List<TopicCategoryVO> selectCategoriesByTopicId(@Param("topicId") Long topicId);
}
