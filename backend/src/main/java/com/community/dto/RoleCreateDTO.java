package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "角色创建请求")
public class RoleCreateDTO {
    @NotBlank(message = "编码不能为空")
    @Schema(description = "角色编码", example = "expert")
    private String code;

    @NotBlank(message = "名称不能为空")
    @Schema(description = "角色名称", example = "Expert")
    private String name;

    @Schema(description = "角色描述")
    private String description;
}
