package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.QaTopic;
import com.community.vo.AdminTopicListItemVO;
import com.community.vo.AppSearchTopicVO;
import com.community.vo.AppTopicListItemVO;
import com.community.vo.TopicQuestionPageItemVO;
import com.community.vo.TopicRecentQuestionVO;
import com.community.vo.TopicTrendPointVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface QaTopicMapper extends BaseMapper<QaTopic> {
    List<AdminTopicListItemVO> selectAdminTopicPage(@Param("title") String title,
                                                    @Param("status") Integer status,
                                                    @Param("createdBy") Long createdBy,
                                                    @Param("dateStart") LocalDate dateStart,
                                                    @Param("dateEnd") LocalDate dateEnd,
                                                    @Param("sortBy") String sortBy,
                                                    @Param("sortOrder") String sortOrder);

    List<TopicRecentQuestionVO> selectRecentQuestionsByTopicId(@Param("topicId") Long topicId,
                                                                @Param("limit") Integer limit);

    List<TopicQuestionPageItemVO> selectTopicQuestionPage(@Param("topicId") Long topicId,
                                                           @Param("status") Integer status,
                                                           @Param("title") String title,
                                                           @Param("sortBy") String sortBy,
                                                           @Param("sortOrder") String sortOrder);

    List<TopicTrendPointVO> selectTopicTrend(@Param("topicId") Long topicId,
                                             @Param("daysMinusOne") Integer daysMinusOne);

    List<AppTopicListItemVO> selectAppTopicPage(@Param("keyword") String keyword,
                                                @Param("sortBy") String sortBy,
                                                @Param("sortOrder") String sortOrder);

    List<AppTopicListItemVO> selectAppHotTopics(@Param("limit") Integer limit);

    List<AppSearchTopicVO> selectAppSearchTopics(@Param("query") String query,
                                                 @Param("limit") Integer limit);

    int updateFollowCount(@Param("id") Long id,
                          @Param("delta") Integer delta);
}
