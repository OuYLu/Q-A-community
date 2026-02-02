package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cms_sensitive_word")
public class CmsSensitiveWord {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String word;

    private Integer level;

    private String category;

    private String hitActionDesc;

    private String reasonTemplate;

    private Integer enabled;

    private LocalDateTime createdAt;
}