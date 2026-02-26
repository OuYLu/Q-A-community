package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.NotifyMessage;
import com.community.vo.AppNotificationItemVO;
import com.community.vo.AppNotificationTypeCountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface NotifyMessageMapper extends BaseMapper<NotifyMessage> {
    List<AppNotificationItemVO> selectAppNotifications(@Param("userId") Long userId,
                                                       @Param("type") Integer type,
                                                       @Param("isRead") Integer isRead);

    List<AppNotificationTypeCountVO> selectUnreadCountByType(@Param("userId") Long userId);

    int updateReadById(@Param("userId") Long userId,
                       @Param("id") Long id,
                       @Param("readAt") LocalDateTime readAt);

    int updateReadAll(@Param("userId") Long userId,
                      @Param("readAt") LocalDateTime readAt);
}
