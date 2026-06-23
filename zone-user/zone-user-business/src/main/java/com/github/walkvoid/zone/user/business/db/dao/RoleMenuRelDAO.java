package com.github.walkvoid.zone.user.business.db.dao;

import com.github.walkvoid.zone.user.business.db.mapper.RoleMenuRelMapper;
import com.github.walkvoid.zone.user.model.entity.RoleMenuRel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色-菜单关联 DAO
 *
 * @author walkvoid
 */
@Repository
public class RoleMenuRelDAO {

    @Autowired
    private RoleMenuRelMapper roleMenuRelMapper;

    public int insert(RoleMenuRel rel) {
        return roleMenuRelMapper.insert(rel);
    }

    public int deleteByRoleId(Long roleId) {
        return roleMenuRelMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RoleMenuRel>()
                .eq("role_id", roleId));
    }

    public int deleteByRoleIdAndMenuId(Long roleId, Long menuId) {
        return roleMenuRelMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RoleMenuRel>()
                .eq("role_id", roleId).eq("menu_id", menuId));
    }

    public List<Long> selectMenuIdsByRoleId(Long roleId) {
        return roleMenuRelMapper.selectMenuIdsByRoleId(roleId);
    }

    public List<RoleMenuRel> selectByRoleId(Long roleId) {
        return roleMenuRelMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RoleMenuRel>()
                .eq("role_id", roleId));
    }

    public int batchInsert(List<RoleMenuRel> rels) {
        int count = 0;
        for (RoleMenuRel rel : rels) {
            count += roleMenuRelMapper.insert(rel);
        }
        return count;
    }
}
