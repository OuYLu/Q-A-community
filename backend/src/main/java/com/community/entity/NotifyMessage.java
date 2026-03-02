package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notify_message")
public class NotifyMessage {
    /**
     * 通知类型：系统通知
     */
    public static final int TYPE_SYSTEM = 1;

    /**
     * 通知类型：收到点赞
     */
    public static final int TYPE_LIKE = 2;

    /**
     * 通知类型：收到收藏
     */
    public static final int TYPE_FAVORITE = 3;

    /**
     * 通知类型：收到关注
     */
    public static final int TYPE_FOLLOW = 4;

    /**
     * 通知类型：收到评论（回答下直接评论）
     */
    public static final int TYPE_COMMENT = 5;

    /**
     * 通知类型：收到回复（评论下楼中楼回复）
     */
    public static final int TYPE_REPLY = 6;

    /**
     * 通知类型：举报反馈/处理结果
     */
    public static final int TYPE_REPORT_FEEDBACK = 7;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long receiverId;

    /**
     * 通知类型：
     * 1-系统通知
     * 2-收到点赞
     * 3-收到收藏
     * 4-收到关注
     * 5-收到评论（回答下直接评论）
     * 6-收到回复（评论下楼中楼回复）
     * 7-举报反馈/处理结果
     */
    private Integer type;

    private Integer bizType;

    private Long bizId;

    private String title;

    private String content;

    private Integer isRead;

    private LocalDateTime createdAt;

    private LocalDateTime readAt;
}
