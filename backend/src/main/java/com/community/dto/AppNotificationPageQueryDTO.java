package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "应用通知分页查询")
public class AppNotificationPageQueryDTO {
    @Schema(description = "按类型筛选")
    private Integer type;

    @Schema(description = "按已读状态筛选：0-未读 1-已读")
    private Integer isRead;

    private Integer page = 1;
    private Integer pageSize = 10;
}


