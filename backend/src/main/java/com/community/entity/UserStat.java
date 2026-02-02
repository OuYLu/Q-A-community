package com.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_stat")
public class UserStat {
    @TableId(value = "user_id")
    private Long userId;

    private Integer questionCount;

    private Integer answerCount;

    private Integer likeReceivedCount;

    private Integer followerCount;

    private Integer followingCount;

    private LocalDateTime updatedAt;
}