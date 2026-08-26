package com.github.walkvoid.zone.system.service;

import java.util.List;

/**
 * 用户权限码查询（菜单 permission）
 */
public interface PermissionService {

    /**
     * 按用户 ID 汇总角色绑定菜单上的 permission 码
     */
    List<String> listPermissionCodesByUserId(Long userId);

    /**
     * 清除用户权限缓存（cache 模式）
     */
    void evictPermissionCache(Long userId);
}
