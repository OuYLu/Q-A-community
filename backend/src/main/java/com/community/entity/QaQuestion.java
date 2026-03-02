package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("qa_question")
public class QaQuestion {
    // 状态：1-已发布，2-待审核，3-驳回，4-下架，5-仅自己可见，6-用户删除
    public static final int STATUS_PUBLISHED = 1;
    public static final int STATUS_PENDING = 2;
    public static final int STATUS_REJECTED = 3;
    public static final int STATUS_OFFLINE = 4;
    public static final int STATUS_SELF_ONLY = 5;
    public static final int STATUS_DELETED_BY_USER = 6;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long categoryId;

    private Long topicId;

    private String title;

    private String content;

    private String imageUrls;

    private Integer status;

    private String rejectReason;

    private Integer viewCount;

    private Integer answerCount;

    private Integer likeCount;

    private Integer favoriteCount;

    private Long acceptedAnswerId;

    private LocalDateTime acceptedAt;

    private LocalDateTime lastActiveAt;

    private Integer deleteFlag;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
