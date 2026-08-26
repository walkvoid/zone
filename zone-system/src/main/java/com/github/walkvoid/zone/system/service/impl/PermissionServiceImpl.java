package com.github.walkvoid.zone.system.service.impl;

import com.github.walkvoid.wvframework.core.security.PermissionCache;
import com.github.walkvoid.zone.system.db.dao.MenuDAO;
import com.github.walkvoid.zone.system.db.entity.Menu;
import com.github.walkvoid.zone.system.service.PermissionService;
import com.github.walkvoid.zone.user.client.RoleFeignClient;
import com.github.walkvoid.zone.user.client.RoleMenuRelFeignClient;
import com.github.walkvoid.zone.user.db.entity.RoleMenuRel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private RoleFeignClient roleService;
    @Autowired
    private RoleMenuRelFeignClient roleMenuRelService;
    @Autowired
    private MenuDAO menuDAO;
    @Autowired
    private ObjectProvider<PermissionCache> permissionCache;

    @Override
    public List<String> listPermissionCodesByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }
        List<String> roleCodes = roleService.getRoleCodesByUserId(userId);
        if (roleCodes == null || roleCodes.isEmpty()) {
            return List.of();
        }

        Set<Long> menuIdSet = new HashSet<>();
        var allRoles = roleService.selectAll();
        List<Long> roleIds = allRoles.stream()
                .filter(r -> roleCodes.contains(r.getRoleCode()))
                .map(r -> r.getId())
                .toList();

        for (Long roleId : roleIds) {
            List<RoleMenuRel> rels = roleMenuRelService.selectByRoleId(roleId);
            if (rels == null) {
                continue;
            }
            rels.stream()
                    .map(RoleMenuRel::getMenuId)
                    .filter(Objects::nonNull)
                    .forEach(menuIdSet::add);
        }

        if (menuIdSet.isEmpty()) {
            return List.of();
        }
        return menuDAO.selectBatchIds(new ArrayList<>(menuIdSet)).stream()
                .map(Menu::getPermission)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public void evictPermissionCache(Long userId) {
        PermissionCache cache = permissionCache.getIfAvailable();
        if (cache != null) {
            cache.evict(userId);
        }
    }
}
