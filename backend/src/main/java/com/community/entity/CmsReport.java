package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cms_report")
public class CmsReport {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Integer bizType;

    private Long bizId;

    private Integer reasonType;

    private Long reporterId;

    private String reasonCode;

    private String reasonDetail;

    private Integer status;

    private Long handlerId;

    private Integer handleAction;

    private String handleResult;

    private LocalDateTime handledAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}