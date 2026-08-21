package com.github.walkvoid.zone.user.controller.internal;

import com.github.walkvoid.zone.user.service.RoleService;
import com.github.walkvoid.zone.user.db.entity.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/role")
public class RoleInternalController {

    @Autowired
    private RoleService roleService;

    @GetMapping("/{id}")
    public Role getById(@PathVariable Long id) {
        return roleService.getById(id);
    }

    @GetMapping("/all")
    public List<Role> selectAll() {
        return roleService.selectAll();
    }

    @PostMapping("/select-list")
    public List<Role> selectList(@RequestBody Role condition) {
        return roleService.selectList(condition);
    }

    @GetMapping("/codes/{userId}")
    public List<String> getRoleCodesByUserId(@PathVariable Long userId) {
        return roleService.getRoleCodesByUserId(userId);
    }
}
