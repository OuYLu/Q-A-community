package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "应用按类型通知数量")
public class AppNotificationTypeCountVO {
    private Integer type;
    private Integer cnt;
}

