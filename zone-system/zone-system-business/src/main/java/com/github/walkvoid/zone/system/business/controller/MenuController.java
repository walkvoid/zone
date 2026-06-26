package com.github.walkvoid.zone.system.business.controller;

import com.github.walkvoid.wvframework.models.WebResponse;
import com.github.walkvoid.zone.system.api.service.MenuCrudService;
import com.github.walkvoid.zone.system.api.service.MenuTreeService;
import com.github.walkvoid.zone.system.model.dto.MenuDTO;
import com.github.walkvoid.zone.system.model.dto.MenuTreeQueryDTO;
import com.github.walkvoid.zone.system.model.vo.MenuTreeNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "系统菜单")
@RestController
@RequestMapping("/system/menu")
public class MenuController {

    @Autowired
    private MenuTreeService menuTreeService;

    @Autowired
    private MenuCrudService menuCrudService;

    @Operation(summary = "获取菜单树（前端动态路由）")
    @GetMapping("/tree")
    public WebResponse<List<MenuTreeNode>> getMenuTree(MenuTreeQueryDTO query) {
        if (query == null) {
            query = new MenuTreeQueryDTO();
        }
        return WebResponse.ok(menuTreeService.getMenuTree(query));
    }

    @Operation(summary = "获取所有菜单树（管理后台）")
    @GetMapping("/all")
    public WebResponse<List<MenuTreeNode>> getAllMenuTree() {
        return WebResponse.ok(menuTreeService.getAllMenuTree());
    }

    @Operation(summary = "获取菜单列表（树形）")
    @GetMapping("/list")
    public WebResponse<List<MenuDTO>> getMenuList() {
        return WebResponse.ok(menuCrudService.getMenuList());
    }

    @Operation(summary = "检查菜单名称是否存在")
    @GetMapping("/name-exists")
    public WebResponse<Boolean> isMenuNameExists(@Parameter(description = "菜单名称") @RequestParam String name,
                                                  @Parameter(description = "排除ID") @RequestParam(required = false) Long id) {
        return WebResponse.ok(menuCrudService.isMenuNameExists(name, id));
    }

    @Operation(summary = "检查菜单路径是否存在")
    @GetMapping("/path-exists")
    public WebResponse<Boolean> isMenuPathExists(@Parameter(description = "菜单路径") @RequestParam String path,
                                                  @Parameter(description = "排除ID") @RequestParam(required = false) Long id) {
        return WebResponse.ok(menuCrudService.isMenuPathExists(path, id));
    }

    @Operation(summary = "创建菜单")
    @PostMapping
    public WebResponse<Void> createMenu(@RequestBody MenuDTO dto) {
        menuCrudService.createMenu(dto);
        return WebResponse.ok(null);
    }

    @Operation(summary = "更新菜单")
    @PutMapping("/{id}")
    public WebResponse<Void> updateMenu(@Parameter(description = "菜单ID") @PathVariable Long id,
                                         @RequestBody MenuDTO dto) {
        menuCrudService.updateMenu(id, dto);
        return WebResponse.ok(null);
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    public WebResponse<Void> deleteMenu(@Parameter(description = "菜单ID") @PathVariable Long id) {
        menuCrudService.deleteMenu(id);
        return WebResponse.ok(null);
    }

    @Operation(summary = "获取菜单分页列表")
    @GetMapping("/page")
    public WebResponse<Map<String, Object>> getMenuPage(
        @Parameter(description = "页码") @RequestParam(name = "page", defaultValue = "1") int page,
        @Parameter(description = "每页大小") @RequestParam(name = "size", defaultValue = "10") int size) {
        return WebResponse.ok(menuCrudService.getMenuPage(page, size));
    }
}
