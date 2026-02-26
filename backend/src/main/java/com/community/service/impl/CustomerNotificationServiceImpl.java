package com.community.service.impl;

import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.common.SecurityUser;
import com.community.dto.AppNotificationPageQueryDTO;
import com.community.mapper.NotifyMessageMapper;
import com.community.service.CustomerNotificationService;
import com.community.vo.AppNotificationItemVO;
import com.community.vo.AppNotificationTypeCountVO;
import com.community.vo.AppNotificationUnreadCountVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerNotificationServiceImpl implements CustomerNotificationService {
    private final NotifyMessageMapper notifyMessageMapper;

    @Override
    public PageInfo<AppNotificationItemVO> page(AppNotificationPageQueryDTO query) {
        Long userId = currentUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "未授权");
        }
        Integer page = query == null || query.getPage() == null ? 1 : query.getPage();
        Integer pageSize = query == null || query.getPageSize() == null ? 10 : query.getPageSize();
        PageHelper.startPage(page, pageSize);
        List<AppNotificationItemVO> list = notifyMessageMapper.selectAppNotifications(
                userId,
                query == null ? null : query.getType(),
                query == null ? null : query.getIsRead()
        );
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
}
