package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("search_query_log")
public class SearchQueryLog {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String queryText;

    private Integer searchType;

    private Integer hitCount;

    private LocalDateTime createdAt;
}