package com.community.service;

import com.community.vo.AppCategoryVO;
import com.community.vo.AppExpertCardVO;
import com.community.vo.AppGuestHomeVO;
import com.community.vo.AppQuestionListItemVO;
import com.community.vo.AppQuestionHotItemVO;
import com.community.vo.AppTopicListItemVO;
import com.github.pagehelper.PageInfo;
import com.community.dto.AppQuestionPageQueryDTO;

import java.util.List;

public interface CustomerDiscoverService {
    AppGuestHomeVO guestHome(Integer topicLimit, Integer questionLimit, Integer expertLimit);
    PageInfo<AppQuestionListItemVO> questionPage(AppQuestionPageQueryDTO query);
    List<AppCategoryVO> listCategories();
    List<AppTopicListItemVO> hotTopics(Integer limit);
    List<AppQuestionHotItemVO> hotQuestions(Integer limit);
    List<AppExpertCardVO> expertCards(Integer limit);
}
