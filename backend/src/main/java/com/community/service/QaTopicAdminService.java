package com.community.service;

import com.community.dto.QaTopicCategoryUpdateDTO;
import com.community.dto.QaTopicPageQueryDTO;
import com.community.dto.QaTopicQuestionPageQueryDTO;
import com.community.dto.QaTopicSaveDTO;
import com.community.dto.QaTopicStatusUpdateDTO;
import com.community.vo.AdminTopicDetailVO;
import com.community.vo.AdminTopicListItemVO;
import com.community.vo.TopicCategoryVO;
import com.community.vo.TopicQuestionPageItemVO;
import com.community.vo.TopicRecentQuestionVO;
import com.community.vo.TopicStatsVO;
import com.community.vo.TopicTrendPointVO;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface QaTopicAdminService {
    PageInfo<AdminTopicListItemVO> page(QaTopicPageQueryDTO query);

    AdminTopicDetailVO detail(Long id);

    Long create(QaTopicSaveDTO dto);

    void update(Long id, QaTopicSaveDTO dto);

    void updateStatus(Long id, QaTopicStatusUpdateDTO dto);

    void delete(Long id);

    List<TopicCategoryVO> listCategories(Long id);

    void updateCategories(Long id, QaTopicCategoryUpdateDTO dto);

    List<TopicRecentQuestionVO> recentQuestions(Long id, Integer limit);

    PageInfo<TopicQuestionPageItemVO> questionPage(Long id, QaTopicQuestionPageQueryDTO query);

    List<TopicTrendPointVO> trend(Long id, Integer days);

    TopicStatsVO stats(Long id);
}
