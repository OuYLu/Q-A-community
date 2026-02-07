package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("permission")
public class Permission {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String code;

    private String name;

    private String type;

    private Long parentId;

    private String pathOrApi;

    private String method;

    private Integer sort;

    private String icon;

    private String component;

    private Integer visible;

    private LocalDateTime createdAt;
}
