package com.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "expert_apply", autoResultMap = true)
public class ExpertApply {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String realName;

    private String organization;

    private String title;

    private String expertise;

    @TableField(value = "proof_urls", typeHandler = JacksonTypeHandler.class)
    private JsonNode proofUrls;

    private Integer status;

    private String rejectReason;

    private Long reviewerId;

    private LocalDateTime reviewAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
