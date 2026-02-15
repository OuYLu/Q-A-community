package com.community.vo;

import lombok.Data;

import java.util.List;
import java.time.LocalDateTime;

@Data
public class UserManageVO {
    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private Integer status;
    private String avatar;
    private List<String> roleCodes;
    private String displayRole;
    private LocalDateTime createdAt;
}
