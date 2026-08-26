package com.github.walkvoid.zone.system.controller.internal;

import com.github.walkvoid.zone.system.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 权限码内部接口（供 Feign / 发 Token / 缓存失效）
 */
@RestController
@RequestMapping("/internal/permission")
public class PermissionInternalController {

    @Autowired
    private PermissionService permissionService;

    @GetMapping("/codes/{userId}")
    public List<String> listCodesByUserId(@PathVariable Long userId) {
        return permissionService.listPermissionCodesByUserId(userId);
    }

    @DeleteMapping("/cache/{userId}")
    public void evictCache(@PathVariable Long userId) {
        permissionService.evictPermissionCache(userId);
    }
}
