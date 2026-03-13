package com.community.service;

import com.community.dto.AppMePasswordChangeDTO;
import com.community.dto.AppMeCancelRequestDTO;
import com.community.dto.AppMePasswordSetFirstDTO;
import com.community.dto.AppMePrivacyUpdateDTO;
import com.community.dto.AppMeProfileUpdateDTO;
import com.community.dto.AppPageQueryDTO;
import com.community.vo.AppMeCancelRequestVO;
import com.community.vo.AppMeDataExportVO;
import com.community.vo.AppMePrivacyVO;
import com.community.vo.AppDocVO;
import com.community.vo.AppFollowTopicItemVO;
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
    PageInfo<AppFollowTopicItemVO> followedTopics(AppPageQueryDTO query);
    AppMePrivacyVO privacy();
    void updatePrivacy(AppMePrivacyUpdateDTO dto);
    AppMeDataExportVO exportData();
    void submitCancelRequest(AppMeCancelRequestDTO dto);
    AppMeCancelRequestVO latestCancelRequest();
    AppDocVO doc(String type);
}
