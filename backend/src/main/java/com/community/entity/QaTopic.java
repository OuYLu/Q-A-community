package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("qa_topic")
public class QaTopic {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String title;

    private String subtitle;

    private String coverImg;

    private String intro;

    private Integer status;

    private Integer followCount;

    private Integer questionCount;

    private Integer todayNewCount;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}