package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kb_category")
public class KbCategory {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long parentId;

    private String name;

    private String description;

    private String icon;

    private Integer sort;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}