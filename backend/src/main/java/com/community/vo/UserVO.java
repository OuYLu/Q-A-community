package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Current user info")
public class UserVO {
    @Schema(description = "User id")
    private Long id;

    @Schema(description = "Username")
    private String username;

    @Schema(description = "Nickname")
    private String nickname;

    @Schema(description = "Role list")
    private List<String> roles;

    @Schema(description = "Role codes")
    private List<String> roleCodes;

    @Schema(description = "Permission codes")
    private List<String> permCodes;

    public UserVO(Long id, String username, String nickname, List<String> roles) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.roles = roles;
    }

    public UserVO(Long id, String username, String nickname, List<String> roles, List<String> roleCodes, List<String> permCodes) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.roles = roles;
        this.roleCodes = roleCodes;
        this.permCodes = permCodes;
    }
}
