package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "应用通知未读数量")
public class AppNotificationUnreadCountVO {
    private Integer total;
    private List<AppNotificationTypeCountVO> byType;
}

