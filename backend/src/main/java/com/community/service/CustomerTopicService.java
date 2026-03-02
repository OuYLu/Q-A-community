package com.community.service;

import com.community.dto.AppTopicPageQueryDTO;
import com.community.dto.AppTopicQuestionQueryDTO;
import com.community.dto.AppQuestionCreateDTO;
import com.community.vo.AppTopicDetailVO;
import com.community.vo.AppTopicListItemVO;
import com.community.vo.AppTopicQuestionItemVO;
import com.github.pagehelper.PageInfo;

public interface CustomerTopicService {
    PageInfo<AppTopicListItemVO> page(AppTopicPageQueryDTO query);
    AppTopicDetailVO detail(Long id);
    PageInfo<AppTopicQuestionItemVO> topicQuestions(Long topicId, AppTopicQuestionQueryDTO query);
    Long createTopicQuestion(Long topicId, AppQuestionCreateDTO dto);
    void follow(Long topicId);
    void unfollow(Long topicId);
}
