package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cms_report")
public class CmsReport {
    // 举报业务类型：1-问题，2-回答，3-评论，4-知识库
    public static final int BIZ_TYPE_QUESTION = 1;
    public static final int BIZ_TYPE_ANSWER = 2;
    public static final int BIZ_TYPE_COMMENT = 3;
    public static final int BIZ_TYPE_KB = 4;

    // 举报处理状态：1-待处理，2-已处理，3-不成立/不处理
    public static final int STATUS_PENDING = 1;
    public static final int STATUS_HANDLED = 2;
    public static final int STATUS_REJECTED = 3;

    // 举报处理动作：1-下架，2-警告，3-封禁，4-不处理
    public static final int HANDLE_ACTION_OFFLINE = 1;
    public static final int HANDLE_ACTION_WARN = 2;
    public static final int HANDLE_ACTION_BAN = 3;
    public static final int HANDLE_ACTION_IGNORE = 4;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 被举报内容类型：1-问题，2-回答，3-评论，4-知识库
     */
    private Integer bizType;

    /**
     * 被举报内容ID
     */
    private Long bizId;

    private Integer reasonType;

    private Long reporterId;

    private String reasonCode;

    private String reasonDetail;

    /**
     * 举报单状态：1-待处理，2-已处理，3-不成立/不处理
     */
    private Integer status;

    private Long handlerId;

    /**
     * 处理动作：1-下架，2-警告，3-封禁，4-不处理
     */
    private Integer handleAction;

    private String handleResult;

    private LocalDateTime handledAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
