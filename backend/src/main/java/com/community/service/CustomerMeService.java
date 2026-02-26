package com.community.service;

import com.community.dto.AppMePasswordChangeDTO;
import com.community.dto.AppMePasswordSetFirstDTO;
import com.community.dto.AppMeProfileUpdateDTO;
import com.community.dto.AppPageQueryDTO;
import com.community.vo.AppDocVO;
import com.community.vo.AppFollowUserItemVO;
import com.community.vo.AppMeOverviewVO;
import com.community.vo.AppMyAnswerItemVO;
import com.community.vo.AppMyFavoriteItemVO;
import com.community.vo.AppMyHistoryItemVO;
import com.community.vo.AppMyQuestionItemVO;
import com.github.pagehelper.PageInfo;

public interface CustomerMeService {
    AppMeOverviewVO overview();
    void updateProfile(AppMeProfileUpdateDTO dto);
    void setFirstPassword(AppMePasswordSetFirstDTO dto);
    void changePassword(AppMePasswordChangeDTO dto);
    PageInfo<AppMyFavoriteItemVO> favorites(AppPageQueryDTO query);
    PageInfo<AppMyHistoryItemVO> history(AppPageQueryDTO query);
    PageInfo<AppMyQuestionItemVO> myQuestions(AppPageQueryDTO query);
    PageInfo<AppMyAnswerItemVO> myAnswers(AppPageQueryDTO query);
    PageInfo<AppFollowUserItemVO> following(AppPageQueryDTO query);
    PageInfo<AppFollowUserItemVO> followers(AppPageQueryDTO query);
    AppDocVO doc(String type);
}
