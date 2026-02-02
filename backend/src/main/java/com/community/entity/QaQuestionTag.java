package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("qa_question_tag")
public class QaQuestionTag {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long questionId;

    private Long tagId;

    private LocalDateTime createdAt;
}