package com.community.service.impl;

import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.common.SecurityUser;
import com.community.dto.AppNotificationPageQueryDTO;
import com.community.mapper.CmsReportMapper;
import com.community.mapper.NotifyMessageMapper;
import com.community.service.CustomerNotificationService;
import com.community.vo.AppNotificationItemVO;
import com.community.vo.AppNotificationTypeCountVO;
import com.community.vo.AppNotificationUnreadCountVO;
import com.community.vo.AppReportFeedbackDetailVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomerNotificationServiceImpl implements CustomerNotificationService {
    private final NotifyMessageMapper notifyMessageMapper;
    private final CmsReportMapper cmsReportMapper;

    @Override
    public PageInfo<AppNotificationItemVO> page(AppNotificationPageQueryDTO query) {
        Long userId = currentUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "未授权");
        }
        Integer page = query == null || query.getPage() == null ? 1 : query.getPage();
        Integer pageSize = query == null || query.getPageSize() == null ? 10 : query.getPageSize();
        PageHelper.startPage(page, Math.min(pageSize, 50));
        List<AppNotificationItemVO> list = notifyMessageMapper.selectAppNotifications(
            userId,
            parseTypes(query),
            query == null ? null : query.getIsRead()
        );
        normalizeReportFeedbackSummary(userId, list);
        return new PageInfo<>(list);
    }

    @Override
    public AppNotificationUnreadCountVO unreadCount() {
        Long userId = currentUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "未授权");
        }
        List<AppNotificationTypeCountVO> rows = notifyMessageMapper.selectUnreadCountByType(userId);
        int total = 0;
        for (AppNotificationTypeCountVO row : rows) {
            if (row != null && row.getCnt() != null) {
                total += row.getCnt();
            }
        }
        AppNotificationUnreadCountVO vo = new AppNotificationUnreadCountVO();
        vo.setTotal(total);
        vo.setByType(rows);
        return vo;
    }

    @Override
    public AppReportFeedbackDetailVO reportFeedbackDetail(Long reportId) {
        Long userId = currentUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "未授权");
        }
        AppReportFeedbackDetailVO detail = cmsReportMapper.selectAppReportFeedbackDetail(reportId, userId);
        if (detail == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "举报反馈不存在或无权限");
        }
        return detail;
    }

    @Override
    @Transactional
    public void readOne(Long id) {
        Long userId = currentUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "未授权");
        }
        int updated = notifyMessageMapper.updateReadById(userId, id, LocalDateTime.now());
        if (updated == 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "通知不存在");
        }
    }

    @Override
    @Transactional
    public void readAll() {
        Long userId = currentUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "未授权");
        }
        notifyMessageMapper.updateReadAll(userId, LocalDateTime.now());
    }

    private List<Integer> parseTypes(AppNotificationPageQueryDTO query) {
        if (query == null) {
            return null;
        }
        Set<Integer> set = new LinkedHashSet<>();
        if (query.getType() != null) {
            set.add(query.getType());
        }
        if (StringUtils.hasText(query.getTypes())) {
            String[] parts = query.getTypes().split(",");
            for (String part : parts) {
                if (!StringUtils.hasText(part)) {
                    continue;
                }
                try {
                    set.add(Integer.parseInt(part.trim()));
                } catch (NumberFormatException ignore) {
                    // ignore invalid type token
                }
            }
        }
        if (set.isEmpty()) {
            return null;
        }
        return new ArrayList<>(set);
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof SecurityUser securityUser) {
            return securityUser.getId();
        }
        return null;
    }

    private void normalizeReportFeedbackSummary(Long userId, List<AppNotificationItemVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (AppNotificationItemVO item : list) {
            if (item == null || item.getType() == null || item.getType() != 7 || item.getBizId() == null) {
                continue;
            }
            Integer bizType = item.getBizType();
            if (bizType == null || (bizType != 1 && bizType != 2 && bizType != 3 && bizType != 4)) {
                continue;
            }
            AppReportFeedbackDetailVO detail = cmsReportMapper.selectAppReportFeedbackDetail(item.getBizId(), userId);
            if (detail == null) {
                continue;
            }
            item.setContent("内容：" + truncateForList(detail.getContentTitle()) + "；处理结果：" + truncateForList(detail.getHandleResult()));
        }
    }

    private String truncateForList(String input) {
        if (!StringUtils.hasText(input)) {
            return "-";
        }
        String s = input.trim();
        return s.length() > 10 ? s.substring(0, 10) + "..." : s;
    }
}
