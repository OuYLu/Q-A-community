package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@TableName("stat_daily")
public class StatDaily {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private LocalDate statDate;

    private Integer questionCnt;

    private Integer answerCnt;

    private Integer commentCnt;

    private Integer activeUserCnt;

    private Integer searchCnt;

    private Integer pendingAuditCnt;

    private LocalDateTime createdAt;
}