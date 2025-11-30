package com.github.walkvoid.zone.user.business.db.dao;

import com.github.walkvoid.zone.user.model.entity.Role;
import com.github.walkvoid.zone.user.business.db.mapper.RoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 角色DAO类
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 * @desc 角色数据访问层类，提供角色相关的数据库操作
 */
@Repository
public class RoleDAO {

    @Autowired
    private RoleMapper roleMapper;

    /**
     * 根据ID查询角色
     * @param id 角色ID
     * @return 角色信息
     */
    public Role selectById(Long id) {
        return roleMapper.selectById(id);
    }

    /**
     * 根据角色代码查询角色
     * @param roleCode 角色代码
     * @return 角色信息
     */
    public Role selectByRoleCode(String roleCode) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Role> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("role_code", roleCode);
        return roleMapper.selectOne(queryWrapper);
    }

    /**
     * 插入角色
     * @param role 角色信息
     * @return 影响行数
     */
    public int insert(Role role) {
        return roleMapper.insert(role);
    }

    /**
     * 更新角色
     * @param role 角色信息
     * @return 影响行数
     */
    public int updateById(Role role) {
        return roleMapper.updateById(role);
    }

    /**
     * 删除角色
     * @param id 角色ID
     * @return 影响行数
     */
    public int deleteById(Long id) {
        return roleMapper.deleteById(id);
    }

    /**
     * 批量删除角色
     * @param ids 角色ID列表
     * @return 影响行数
     */
    public int deleteBatchIds(List<Long> ids) {
        return roleMapper.deleteBatchIds(ids);
    }

    /**
     * 根据用户ID查询角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    public List<Role> selectRolesByUserId(Long userId) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Role> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.inSql("id", "select role_id from user_role where user_id = " + userId);
        return roleMapper.selectList(queryWrapper);
    }

    /**
     * 查询所有可用的角色
     * @return 角色列表
     */
    public List<Role> selectAvailableRoles() {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Role> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("status", 1); // 假设1表示可用状态
        return roleMapper.selectList(queryWrapper);
    }

    /**
     * 查询非系统角色
     * @return 角色列表
     */
    public List<Role> selectNonSystemRoles() {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Role> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("is_system", 0); // 假设0表示非系统角色
        return roleMapper.selectList(queryWrapper);
    }

    /**
     * 查询所有角色
     * @return 角色列表
     */
    public List<Role> selectAll() {
        return roleMapper.selectList(null);
    }

    /**
     * 根据条件查询角色列表
     * @param role 查询条件
     * @return 角色列表
     */
    public List<Role> selectList(Role role) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Role> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>(role);
        return roleMapper.selectList(queryWrapper);
    }

    /**
     * 统计角色数量
     * @return 角色数量
     */
    public long count() {
        return roleMapper.selectCount(null);
    }

    /**
     * 根据条件统计角色数量
     * @param role 查询条件
     * @return 角色数量
     */
    public long countByCondition(Role role) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Role> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>(role);
        return roleMapper.selectCount(queryWrapper);
    }

    /**
     * 检查角色代码是否存在
     * @param roleCode 角色代码
     * @return 存在数量
     */
    public long checkRoleCodeExists(String roleCode) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Role> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("role_code", roleCode);
        return roleMapper.selectCount(queryWrapper);
    }

    /**
     * 检查角色名称是否存在
     * @param roleName 角色名称
     * @return 存在数量
     */
    public long checkRoleNameExists(String roleName) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Role> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("role_name", roleName);
        return roleMapper.selectCount(queryWrapper);
    }
}