package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("qa_vote")
public class QaVote {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Integer bizType;

    private Long bizId;

    private Long userId;

    private Integer voteType;

    private LocalDateTime createdAt;
}