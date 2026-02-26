package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.QaQuestionTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QaQuestionTagMapper extends BaseMapper<QaQuestionTag> {
    List<String> selectTagNamesByQuestionId(@Param("questionId") Long questionId);
}
