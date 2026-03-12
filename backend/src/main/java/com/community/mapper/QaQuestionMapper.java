package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.QaQuestion;
import com.community.vo.AppQuestionDetailVO;
import com.community.vo.AppMyQuestionItemVO;
import com.community.vo.AppQuestionHotItemVO;
import com.community.vo.AppQuestionListItemVO;
import com.community.vo.AppSearchQuestionVO;
import com.community.vo.AppTopicQuestionItemVO;
import com.community.vo.SearchQuestionDoc;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QaQuestionMapper extends BaseMapper<QaQuestion> {
    List<AppQuestionHotItemVO> selectAppHotQuestions(@Param("limit") Integer limit);

    List<AppTopicQuestionItemVO> selectAppTopicQuestions(@Param("topicId") Long topicId,
                                                         @Param("sortBy") String sortBy,
                                                         @Param("onlyUnsolved") boolean onlyUnsolved);

    List<AppSearchQuestionVO> selectAppSearchQuestions(@Param("query") String query,
                                                       @Param("sortBy") String sortBy,
                                                       @Param("categoryId") Long categoryId,
                                                       @Param("topicId") Long topicId,
                                                       @Param("onlyUnsolved") Boolean onlyUnsolved,
                                                       @Param("limit") Integer limit,
                                                       @Param("offset") Integer offset);

    List<AppSearchQuestionVO> selectAppSearchQuestionsByIds(@Param("ids") List<Long> ids,
                                                            @Param("categoryId") Long categoryId,
                                                            @Param("topicId") Long topicId,
                                                            @Param("onlyUnsolved") Boolean onlyUnsolved);

    List<SearchQuestionDoc> selectSearchQuestionDocs();

    SearchQuestionDoc selectSearchQuestionDocById(@Param("id") Long id);

    List<AppMyQuestionItemVO> selectMyQuestions(@Param("userId") Long userId);

    List<AppQuestionListItemVO> selectAppQuestionPage(@Param("keyword") String keyword,
                                                      @Param("categoryId") Long categoryId,
                                                      @Param("topicId") Long topicId,
                                                      @Param("sortBy") String sortBy,
                                                      @Param("onlyUnsolved") Boolean onlyUnsolved,
                                                      @Param("userId") Long userId,
                                                      @Param("personalized") Boolean personalized);

    AppQuestionDetailVO selectAppQuestionDetail(@Param("id") Long id);

    int incrementViewCount(@Param("id") Long id);

    int updateAnswerCount(@Param("id") Long id,
                          @Param("delta") Integer delta);
}
