package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "角色更新请求")
public class RoleUpdateDTO {
    @NotBlank(message = "名称不能为空")
    @Schema(description = "角色名称", example = "Expert")
    private String name;
}
