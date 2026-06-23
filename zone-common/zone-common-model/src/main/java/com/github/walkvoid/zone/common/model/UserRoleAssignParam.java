package com.github.walkvoid.zone.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户-角色分配请求
 *
 * @author walkvoid
 */
@Data
@Schema(description = "用户角色分配请求")
public class UserRoleAssignParam {

    @Schema(description = "角色ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> roleIds;
}
