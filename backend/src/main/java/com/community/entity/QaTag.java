package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("qa_tag")
public class QaTag {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private Integer source;

    private Integer status;

    private Integer useCount;

    private LocalDateTime createdAt;
}
