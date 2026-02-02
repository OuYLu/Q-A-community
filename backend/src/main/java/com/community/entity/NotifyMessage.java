package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notify_message")
public class NotifyMessage {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long receiverId;

    private Integer type;

    private Integer bizType;

    private Long bizId;

    private String title;

    private String content;

    private Integer isRead;

    private LocalDateTime createdAt;

    private LocalDateTime readAt;
}