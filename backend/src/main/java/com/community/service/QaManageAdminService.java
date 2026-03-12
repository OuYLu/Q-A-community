package com.community.service;

import com.community.dto.QaManageAnswerPageQueryDTO;
import com.community.dto.QaManageQuestionPageQueryDTO;
import com.community.dto.QaManageStatusUpdateDTO;
import com.community.vo.AdminQaAnswerPageItemVO;
import com.community.vo.AdminQaQuestionPageItemVO;
import com.github.pagehelper.PageInfo;

public interface QaManageAdminService {
    PageInfo<AdminQaQuestionPageItemVO> questionPage(QaManageQuestionPageQueryDTO query);

    PageInfo<AdminQaAnswerPageItemVO> answerPage(QaManageAnswerPageQueryDTO query);

    void updateQuestionStatus(Long id, QaManageStatusUpdateDTO dto);

    void updateAnswerStatus(Long id, QaManageStatusUpdateDTO dto);

    void deleteQuestion(Long id);

    void deleteAnswer(Long id);
}
