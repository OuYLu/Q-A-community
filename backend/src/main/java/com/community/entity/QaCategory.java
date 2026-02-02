package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("qa_category")
public class QaCategory {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private Long parentId;

    private String icon;

    private String description;

    private Integer status;

    private Integer sort;

    private Integer deleteFlag;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}