package com.github.walkvoid.zone.user.business.service.impl;

import com.github.walkvoid.zone.user.api.service.RoleService;
import com.github.walkvoid.zone.user.business.db.dao.RoleDAO;
import com.github.walkvoid.zone.user.business.db.dao.UserRoleRelDAO;
import com.github.walkvoid.zone.user.model.entity.Role;
import com.github.walkvoid.zone.user.model.entity.UserRoleRel;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@DubboService
@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleDAO roleDAO;

    @Autowired
    private UserRoleRelDAO userRoleRelDAO;

    @Override
    public Role getById(Long id) {
        return roleDAO.selectById(id);
    }

    @Override
    public List<Role> selectAll() {
        return roleDAO.selectAll();
    }

    @Override
    public List<Role> selectList(Role condition) {
        return roleDAO.selectList(condition);
    }

    @Override
    public List<String> getRoleCodesByUserId(Long userId) {
        if (userId == null) return List.of();
        return userRoleRelDAO.selectByUserId(userId).stream()
                .map(rel -> roleDAO.selectById(rel.getRoleId()))
                .filter(Objects::nonNull)
                .map(Role::getRoleCode)
                .filter(Objects::nonNull)
                .toList();
    }
}
