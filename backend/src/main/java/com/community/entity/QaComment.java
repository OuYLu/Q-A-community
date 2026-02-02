package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("qa_comment")
public class QaComment {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Integer bizType;

    private Long bizId;

    private Long userId;

    private Long parentId;

    private String content;

    private Integer status;

    private String rejectReason;

    private Integer deleteFlag;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}