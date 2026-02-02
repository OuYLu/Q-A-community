package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("qa_topic_follow")
public class QaTopicFollow {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long topicId;

    private Long userId;

    private LocalDateTime createdAt;
}