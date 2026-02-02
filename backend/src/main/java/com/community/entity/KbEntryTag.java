package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kb_entry_tag")
public class KbEntryTag {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long entryId;

    private Long tagId;

    private LocalDateTime createdAt;
}