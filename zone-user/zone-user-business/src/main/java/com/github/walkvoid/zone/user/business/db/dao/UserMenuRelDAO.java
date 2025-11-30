package com.github.walkvoid.zone.user.business.db.dao;

import com.github.walkvoid.zone.user.model.entity.Menu;
import com.github.walkvoid.zone.user.model.entity.Role;
import com.github.walkvoid.zone.user.business.db.mapper.UserMenuRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

/**
 * 用户菜单角色关联DAO类
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 * @desc 用户菜单角色关联相关数据访问类（主要用于联表查询）
 */
@Repository
public class UserMenuRelDAO {

    @Autowired
    private UserMenuRoleMapper userMenuRoleMapper;

    /**
     * 根据用户ID查询菜单和角色关联信息
     * @param userId 用户ID
     * @return 菜单角色关联信息列表
     */
    public List<Map<String, Object>> selectUserMenuRoleByUserId(Long userId) {
        // 这里需要自定义SQL查询，暂时使用原生SQL方式
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper();
        queryWrapper.eq("u.id", userId);
        return userMenuRoleMapper.selectUserMenuRoleByUserId(userId);
    }

    /**
     * 根据角色ID查询菜单列表（包含权限信息）
     * @param roleId 角色ID
     * @return 菜单列表
     */
    public List<Menu> selectMenusWithPermissionByRoleId(Long roleId) {
        return userMenuRoleMapper.selectMenusWithPermissionByRoleId(roleId);
    }



}