package com.github.walkvoid.zone.user.api.service;

import com.github.walkvoid.zone.user.model.entity.Role;

import java.util.List;

/**
 * 角色服务接口
 *
 * @author walkvoid
 */
public interface RoleService {

    Role getById(Long id);

    List<Role> selectAll();

    List<Role> selectList(Role condition);

    /**
     * 获取用户的角色编码列表
     */
    List<String> getRoleCodesByUserId(Long userId);
}
