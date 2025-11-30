package com.github.walkvoid.zone.user.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.walkvoid.zone.user.business.db.mapper.MenuMapper;
import com.github.walkvoid.zone.user.model.entity.Menu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 菜单DAO类
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 * @desc 菜单数据访问层类，提供菜单相关的数据库操作
 */
@Repository
public class MenuDAO {

    @Autowired
    private MenuMapper menuMapper;

    /**
     * 根据ID查询菜单
     * @param id 菜单ID
     * @return 菜单信息
     */
    public Menu selectById(Long id) {
        return menuMapper.selectById(id);
    }

    /**
     * 插入菜单
     * @param menu 菜单信息
     * @return 影响行数
     */
    public int insert(Menu menu) {
        return menuMapper.insert(menu);
    }

    /**
     * 更新菜单
     * @param menu 菜单信息
     * @return 影响行数
     */
    public int updateById(Menu menu) {
        return menuMapper.updateById(menu);
    }

    /**
     * 删除菜单
     * @param id 菜单ID
     * @return 影响行数
     */
    public int deleteById(Long id) {
        return menuMapper.deleteById(id);
    }

    /**
     * 批量删除菜单
     * @param ids 菜单ID列表
     * @return 影响行数
     */
    public int deleteByIds(List<Long> ids) {
        return menuMapper.deleteByIds(ids);
    }

    /**
     * 根据角色ID查询菜单列表
     * @param roleId 角色ID
     * @return 菜单列表
     */
    public List<Menu> selectMenusByRoleId(Long roleId) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Menu> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.inSql("id", "select menu_id from role_menu where role_id = " + roleId);
        return menuMapper.selectList(queryWrapper);
    }

    /**
     * 根据用户ID查询菜单列表
     * @param userId 用户ID
     * @return 菜单列表
     */
    public List<Menu> selectMenusByUserId(Long userId) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Menu> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.inSql("id", "select menu_id from role_menu where role_id in (select role_id from user_role where user_id = " + userId + ")");
        return menuMapper.selectList(queryWrapper);
    }

    /**
     * 根据父菜单ID查询子菜单
     * @param parentId 父菜单ID
     * @return 子菜单列表
     */
    public List<Menu> selectChildMenusByParentId(Long parentId) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Menu> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("parent_id", parentId);
        return menuMapper.selectList(queryWrapper);
    }

    /**
     * 查询所有启用的菜单
     * @return 菜单列表
     */
    public List<Menu> selectAllEnabledMenus() {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Menu> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("status", 1).orderByAsc("sort");
        return menuMapper.selectList(queryWrapper);
    }

    /**
     * 查询所有菜单
     * @return 菜单列表
     */
    public List<Menu> selectAll() {
        return menuMapper.selectList(null);
    }

    /**
     * 根据条件查询菜单列表
     * @param menu 查询条件
     * @return 菜单列表
     */
    public List<Menu> selectList(Menu menu) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Menu> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>(menu);
        return menuMapper.selectList(queryWrapper);
    }

    /**
     * 统计菜单数量
     * @return 菜单数量
     */
    public long count() {
        return menuMapper.selectCount(new QueryWrapper<>());
    }

    /**
     * 根据条件统计菜单数量
     * @param menu 查询条件
     * @return 菜单数量
     */
    public int countByCondition(Menu menu) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Menu> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>(menu);
        return Math.toIntExact(menuMapper.selectCount(queryWrapper));
    }
}