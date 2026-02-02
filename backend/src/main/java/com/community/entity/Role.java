package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("role")
public class Role {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String code;

    private String name;

    private String description;

    private LocalDateTime createdAt;
}