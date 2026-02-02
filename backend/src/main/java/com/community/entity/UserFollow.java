package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_follow")
public class UserFollow {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long followerId;

    private Long followeeId;

    private LocalDateTime createdAt;
}