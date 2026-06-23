package com.github.walkvoid.zone.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 角色-菜单分配请求
 *
 * @author walkvoid
 */
@Data
@Schema(description = "角色菜单分配请求")
public class RoleMenuAssignParam {

    @Schema(description = "菜单ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> menuIds;
}
