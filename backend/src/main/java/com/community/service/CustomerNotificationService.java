package com.community.service;

import com.community.dto.AppNotificationPageQueryDTO;
import com.community.vo.AppNotificationItemVO;
import com.community.vo.AppReportFeedbackDetailVO;
import com.community.vo.AppNotificationUnreadCountVO;
import com.github.pagehelper.PageInfo;

public interface CustomerNotificationService {
    PageInfo<AppNotificationItemVO> page(AppNotificationPageQueryDTO query);
    AppNotificationUnreadCountVO unreadCount();
    AppReportFeedbackDetailVO reportFeedbackDetail(Long reportId);
    void readOne(Long id);
    void readAll();
}
