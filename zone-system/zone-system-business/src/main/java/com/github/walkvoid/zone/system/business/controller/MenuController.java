package com.github.walkvoid.zone.system.business.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.models.ApiResult;
import com.github.walkvoid.wvframework.models.PageRequest;
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
    public ApiResult<List<MenuTreeNode>> getMenuTree(MenuTreeQueryDTO query) {
        if (query == null) {
            query = new MenuTreeQueryDTO();
        }
        return ApiResult.ok(menuTreeService.getMenuTree(query));
    }

    @Operation(summary = "获取所有菜单树（管理后台）")
    @GetMapping("/all")
    public ApiResult<List<MenuTreeNode>> getAllMenuTree() {
        return ApiResult.ok(menuTreeService.getAllMenuTree());
    }

    @Operation(summary = "获取菜单列表（树形）")
    @GetMapping("/list")
    public ApiResult<List<MenuDTO>> getMenuList() {
        return ApiResult.ok(menuCrudService.getMenuList());
    }

    @Operation(summary = "检查菜单名称是否存在")
    @GetMapping("/name-exists")
    public ApiResult<Boolean> isMenuNameExists(@Parameter(description = "菜单名称") @RequestParam String name,
                                                  @Parameter(description = "排除ID") @RequestParam(required = false) Long id) {
        return ApiResult.ok(menuCrudService.isMenuNameExists(name, id));
    }

    @Operation(summary = "检查菜单路径是否存在")
    @GetMapping("/path-exists")
    public ApiResult<Boolean> isMenuPathExists(@Parameter(description = "菜单路径") @RequestParam String path,
                                                  @Parameter(description = "排除ID") @RequestParam(required = false) Long id) {
        return ApiResult.ok(menuCrudService.isMenuPathExists(path, id));
    }

    @Operation(summary = "创建菜单")
    @PostMapping
    public ApiResult<String> createMenu(@RequestBody MenuDTO dto) {
        menuCrudService.createMenu(dto);
        return ApiResult.ok("OK");
    }

    @Operation(summary = "更新菜单")
    @PutMapping("/{id}")
    public ApiResult<String> updateMenu(@Parameter(description = "菜单ID") @PathVariable("id") Long id,
                                         @RequestBody MenuDTO dto) {
        menuCrudService.updateMenu(id, dto);
        return ApiResult.ok("OK");
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    public ApiResult<String> deleteMenu(@Parameter(description = "菜单ID") @PathVariable("id") Long id) {
        menuCrudService.deleteMenu(id);
        return ApiResult.ok("OK");
    }

    @Operation(summary = "获取菜单分页列表")
    @GetMapping("/page")
    public ApiResult<PageDTO<MenuDTO>> page(
            @RequestParam(value = "current", defaultValue = "0") long current,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        PageRequest<Void> pageRequest = PageRequest.of(current, size, null);
        PageDTO<MenuDTO> pageResult = menuCrudService.page(pageRequest);
        return ApiResult.ok(pageResult);
    }
}
