package com.github.walkvoid.zone.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.walkvoid.zone.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色-菜单关联实体
 *
 * @author walkvoid
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("role_menu_rel")
public class RoleMenuRel extends BaseEntity {

    @TableId
    private Long id;

    /** 角色ID */
    private Long roleId;

    /** 菜单ID */
    private Long menuId;
}
