package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "当前用户信息")
public class UserVO {
    @Schema(description = "用户编号")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "角色列表")
    private List<String> roles;

    @Schema(description = "角色编码列表")
    private List<String> roleCodes;

    @Schema(description = "权限编码列表")
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
