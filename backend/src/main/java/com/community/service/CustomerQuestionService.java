package com.community.service;

import com.community.dto.AppAnswerCreateDTO;
import com.community.dto.AppAnswerCommentCreateDTO;
import com.community.dto.AppAnswerUpdateDTO;
import com.community.dto.AppPageQueryDTO;
import com.community.dto.AppQuestionCreateDTO;
import com.community.dto.AppQuestionPageQueryDTO;
import com.community.dto.AppQuestionUpdateDTO;
import com.community.vo.AppMyQuestionItemVO;
import com.community.vo.AppAnswerDetailVO;
import com.community.vo.AppAnswerCommentVO;
import com.community.vo.AppQuestionDetailVO;
import com.community.vo.AppQuestionListItemVO;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface CustomerQuestionService {
    PageInfo<AppQuestionListItemVO> page(AppQuestionPageQueryDTO query);

    PageInfo<AppMyQuestionItemVO> myQuestions(AppPageQueryDTO query);

    AppQuestionDetailVO detail(Long id);

    Long createQuestion(AppQuestionCreateDTO dto);

    void updateQuestion(Long id, AppQuestionUpdateDTO dto);

    void deleteQuestion(Long id);

    Long createAnswer(Long questionId, AppAnswerCreateDTO dto);

    void updateAnswer(Long answerId, AppAnswerUpdateDTO dto);

    void deleteAnswer(Long answerId);

    AppQuestionDetailVO toggleQuestionLike(Long questionId);

    AppQuestionDetailVO toggleQuestionFavorite(Long questionId);

    AppAnswerDetailVO answerDetail(Long answerId);

    AppAnswerDetailVO toggleAnswerLike(Long answerId);

    AppAnswerDetailVO toggleAnswerFavorite(Long answerId);

    void recommendBestAnswer(Long questionId, Long answerId);

    List<AppAnswerCommentVO> answerComments(Long answerId);

    Long createAnswerComment(Long answerId, AppAnswerCommentCreateDTO dto);
}
