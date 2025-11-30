package com.github.walkvoid.zone.user.business.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.walkvoid.zone.user.model.entity.Menu;
import com.github.walkvoid.zone.user.model.entity.UserMenuRel;

import java.util.List;
import java.util.Map;

/**
 * 用户菜单角色关联Mapper接口
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 * @desc 用户菜单角色关联相关数据访问映射接口
 */
public interface UserMenuRoleMapper extends BaseMapper<UserMenuRel> {
    List<Map<String, Object>> selectUserMenuRoleByUserId(Long userId);

    List<Menu> selectMenusWithPermissionByRoleId(Long roleId);
}