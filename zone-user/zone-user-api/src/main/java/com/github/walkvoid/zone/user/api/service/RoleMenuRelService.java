package com.github.walkvoid.zone.user.api.service;

import com.github.walkvoid.zone.user.model.entity.RoleMenuRel;

import java.util.List;

/**
 * 角色-菜单关联服务接口
 *
 * @author walkvoid
 */
public interface RoleMenuRelService {

    int insert(RoleMenuRel rel);

    int deleteByRoleId(Long roleId);

    int deleteByMenuId(Long menuId);

    int deleteByRoleIdAndMenuId(Long roleId, Long menuId);

    List<Long> selectMenuIdsByRoleId(Long roleId);

    List<RoleMenuRel> selectByRoleId(Long roleId);
}
