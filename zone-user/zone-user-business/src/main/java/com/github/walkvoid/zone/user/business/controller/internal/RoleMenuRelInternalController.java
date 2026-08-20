package com.github.walkvoid.zone.user.business.controller.internal;

import com.github.walkvoid.zone.user.api.service.RoleMenuRelService;
import com.github.walkvoid.zone.user.model.entity.RoleMenuRel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/role-menu-rel")
public class RoleMenuRelInternalController {

    @Autowired
    private RoleMenuRelService roleMenuRelService;

    @PostMapping
    public int insert(@RequestBody RoleMenuRel rel) {
        return roleMenuRelService.insert(rel);
    }

    @DeleteMapping("/by-role/{roleId}")
    public int deleteByRoleId(@PathVariable Long roleId) {
        return roleMenuRelService.deleteByRoleId(roleId);
    }

    @DeleteMapping("/by-menu/{menuId}")
    public int deleteByMenuId(@PathVariable Long menuId) {
        return roleMenuRelService.deleteByMenuId(menuId);
    }

    @DeleteMapping("/by-role/{roleId}/menu/{menuId}")
    public int deleteByRoleIdAndMenuId(@PathVariable Long roleId, @PathVariable Long menuId) {
        return roleMenuRelService.deleteByRoleIdAndMenuId(roleId, menuId);
    }

    @GetMapping("/menu-ids/{roleId}")
    public List<Long> selectMenuIdsByRoleId(@PathVariable Long roleId) {
        return roleMenuRelService.selectMenuIdsByRoleId(roleId);
    }

    @GetMapping("/by-role/{roleId}")
    public List<RoleMenuRel> selectByRoleId(@PathVariable Long roleId) {
        return roleMenuRelService.selectByRoleId(roleId);
    }
}
