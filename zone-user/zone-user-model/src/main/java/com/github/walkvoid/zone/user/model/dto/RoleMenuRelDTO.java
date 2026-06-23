package com.github.walkvoid.zone.user.model.dto;

import com.github.walkvoid.zone.common.model.BaseDTO;
import lombok.Data;

/**
 * 角色-菜单关联 DTO
 *
 * @author walkvoid
 */
@Data
public class RoleMenuRelDTO extends BaseDTO {

    private Long id;

    /** 角色ID */
    private Long roleId;

    /** 菜单ID */
    private Long menuId;
}
