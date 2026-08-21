package com.github.walkvoid.zone.user.service.impl;

import com.github.walkvoid.zone.user.service.RoleMenuRelService;
import com.github.walkvoid.zone.user.db.dao.RoleMenuRelDAO;
import com.github.walkvoid.zone.user.db.entity.RoleMenuRel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleMenuRelServiceImpl implements RoleMenuRelService {

    @Autowired
    private RoleMenuRelDAO roleMenuRelDAO;

    @Override
    public int insert(RoleMenuRel rel) {
        return roleMenuRelDAO.insert(rel);
    }

    @Override
    public int deleteByRoleId(Long roleId) {
        return roleMenuRelDAO.deleteByRoleId(roleId);
    }

    @Override
    public int deleteByMenuId(Long menuId) {
        return roleMenuRelDAO.deleteByMenuId(menuId);
    }

    @Override
    public int deleteByRoleIdAndMenuId(Long roleId, Long menuId) {
        return roleMenuRelDAO.deleteByRoleIdAndMenuId(roleId, menuId);
    }

    @Override
    public List<Long> selectMenuIdsByRoleId(Long roleId) {
        return roleMenuRelDAO.selectMenuIdsByRoleId(roleId);
    }

    @Override
    public List<RoleMenuRel> selectByRoleId(Long roleId) {
        return roleMenuRelDAO.selectByRoleId(roleId);
    }
}
