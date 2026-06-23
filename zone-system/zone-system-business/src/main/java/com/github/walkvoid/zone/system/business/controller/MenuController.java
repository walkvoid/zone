package com.github.walkvoid.zone.system.business.controller;

import com.github.walkvoid.zone.system.api.service.MenuCrudService;
import com.github.walkvoid.zone.system.api.service.MenuTreeService;
import com.github.walkvoid.zone.system.model.dto.MenuDTO;
import com.github.walkvoid.zone.system.model.query.MenuTreeQuery;
import com.github.walkvoid.zone.system.model.vo.MenuTreeNode;
import com.github.walkvoid.zone.system.model.vo.MenuVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统菜单管理
 *
 * @author walkvoid
 */
@Tag(name = "系统菜单")
@RestController
@RequestMapping("/system/menu")
public class MenuController {

    @Autowired
    private MenuTreeService menuTreeService;

    @Autowired
    private MenuCrudService menuCrudService;

    // ==================== 动态路由树 ====================

    @Operation(summary = "获取菜单树（前端动态路由）")
    @GetMapping("/tree")
    public List<MenuTreeNode> getMenuTree(MenuTreeQuery query) {
        if (query == null) {
            query = new MenuTreeQuery();
        }
        return menuTreeService.getMenuTree(query);
    }

    @Operation(summary = "获取所有菜单树（管理后台）")
    @GetMapping("/all")
    public List<MenuTreeNode> getAllMenuTree() {
        return menuTreeService.getAllMenuTree();
    }

    // ==================== 后台管理 CRUD ====================

    @Operation(summary = "获取菜单列表（树形）")
    @GetMapping("/list")
    public List<MenuVO> getMenuList() {
        return menuCrudService.getMenuList();
    }

    @Operation(summary = "检查菜单名称是否存在")
    @GetMapping("/name-exists")
    public boolean isMenuNameExists(@Parameter(description = "菜单名称") @RequestParam String name,
                                     @Parameter(description = "排除ID") @RequestParam(required = false) Long id) {
        return menuCrudService.isMenuNameExists(name, id);
    }

    @Operation(summary = "检查菜单路径是否存在")
    @GetMapping("/path-exists")
    public boolean isMenuPathExists(@Parameter(description = "菜单路径") @RequestParam String path,
                                     @Parameter(description = "排除ID") @RequestParam(required = false) Long id) {
        return menuCrudService.isMenuPathExists(path, id);
    }

    @Operation(summary = "创建菜单")
    @PostMapping
    public void createMenu(@RequestBody MenuDTO dto) {
        menuCrudService.createMenu(dto);
    }

    @Operation(summary = "更新菜单")
    @PutMapping("/{id}")
    public void updateMenu(@Parameter(description = "菜单ID") @PathVariable Long id,
                            @RequestBody MenuDTO dto) {
        menuCrudService.updateMenu(id, dto);
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    public void deleteMenu(@Parameter(description = "菜单ID") @PathVariable Long id) {
        menuCrudService.deleteMenu(id);
    }

    @Operation(summary = "获取菜单分页列表")
    @GetMapping("/page")
    public Map<String, Object> getMenuPage(
        @Parameter(description = "页码") @RequestParam(name = "page", defaultValue = "1") int page,
        @Parameter(description = "每页大小") @RequestParam(name = "size", defaultValue = "10") int size) {
        return menuCrudService.getMenuPage(page, size);
    }
}
