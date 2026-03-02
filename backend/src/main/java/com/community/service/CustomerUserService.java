package com.community.service;

import com.community.vo.AppUserHomeVO;
import com.community.vo.AppMyAnswerItemVO;
import com.community.vo.AppExpertPostItemVO;
import com.community.dto.AppPageQueryDTO;
import com.github.pagehelper.PageInfo;

public interface CustomerUserService {
    AppUserHomeVO home(Long userId);

    void follow(Long userId);

    void unfollow(Long userId);

    PageInfo<AppMyAnswerItemVO> answers(Long userId, AppPageQueryDTO query);

    PageInfo<AppExpertPostItemVO> expertPosts(Long userId, AppPageQueryDTO query);
}
