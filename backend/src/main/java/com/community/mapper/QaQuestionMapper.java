package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.QaQuestion;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QaQuestionMapper extends BaseMapper<QaQuestion> {
}