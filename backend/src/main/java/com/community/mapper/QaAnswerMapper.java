package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.QaAnswer;
import com.community.vo.AppMyAnswerItemVO;
import com.community.vo.AppQuestionAnswerVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QaAnswerMapper extends BaseMapper<QaAnswer> {
    List<AppMyAnswerItemVO> selectMyAnswers(@Param("userId") Long userId);

    List<AppMyAnswerItemVO> selectUserEffectiveAnswers(@Param("userId") Long userId);

    List<AppQuestionAnswerVO> selectAppQuestionAnswers(@Param("questionId") Long questionId);

    AppQuestionAnswerVO selectAppAnswerById(@Param("answerId") Long answerId);

    Long sumLikeCountByUserId(@Param("userId") Long userId);

    Long countUserEffectiveAnswers(@Param("userId") Long userId);

    Long countMyEffectiveAnswers(@Param("userId") Long userId);
}
