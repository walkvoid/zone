package com.github.walkvoid.zone.system.api.service;

import com.github.walkvoid.zone.system.model.dto.MenuDTO;

import java.util.List;
import java.util.Map;

/**
 * 系统菜单 CRUD 服务接口
 *
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 */
public interface MenuCrudService {

    /**
     * 获取菜单列表（含树形结构）
     */
    List<MenuDTO> getMenuList();

    /**
     * 创建菜单
     */
    void createMenu(MenuDTO dto);

    /**
     * 更新菜单
     */
    void updateMenu(Long id, MenuDTO dto);

    /**
     * 删除菜单
     */
    void deleteMenu(Long id);

    /**
     * 检查菜单名称是否存在
     */
    boolean isMenuNameExists(String name, Long excludeId);

    /**
     * 检查菜单路径是否存在
     */
    boolean isMenuPathExists(String path, Long excludeId);
    /**
     * 获取菜单分页列表
     */
    Map<String, Object> getMenuPage(int page, int size);
}
