package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kb_entry")
public class KbEntry {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long categoryId;

    private String title;

    private String summary;

    private String content;

    private String contentRef;

    private String source;

    private Long authorUserId;

    private Integer status;

    private Integer viewCount;

    private Integer likeCount;

    private Integer favoriteCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}