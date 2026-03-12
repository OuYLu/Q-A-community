package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.QaAnswer;
import com.community.vo.AppMyAnswerItemVO;
import com.community.vo.AppQuestionAnswerVO;
import com.community.vo.AppSearchAnswerVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QaAnswerMapper extends BaseMapper<QaAnswer> {
    List<AppMyAnswerItemVO> selectMyAnswers(@Param("userId") Long userId);

    List<AppMyAnswerItemVO> selectUserEffectiveAnswers(@Param("userId") Long userId);

    List<AppQuestionAnswerVO> selectAppQuestionAnswers(@Param("questionId") Long questionId);

    AppQuestionAnswerVO selectAppAnswerById(@Param("answerId") Long answerId);

    List<AppSearchAnswerVO> selectAppSearchAnswers(@Param("query") String query,
                                                   @Param("semanticTerms") List<String> semanticTerms,
                                                   @Param("sortBy") String sortBy,
                                                   @Param("limit") Integer limit,
                                                   @Param("offset") Integer offset);

    Long sumLikeCountByUserId(@Param("userId") Long userId);

    Long countUserEffectiveAnswers(@Param("userId") Long userId);

    Long countMyEffectiveAnswers(@Param("userId") Long userId);
}
