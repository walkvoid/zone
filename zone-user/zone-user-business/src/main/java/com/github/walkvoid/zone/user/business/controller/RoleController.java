package com.github.walkvoid.zone.user.business.controller;

import com.github.walkvoid.wvframework.models.WebResponse;
import com.github.walkvoid.wvframework.utils.BeanCopyUtils;
import com.github.walkvoid.zone.common.model.IdsParam;
import com.github.walkvoid.zone.common.model.RoleMenuAssignParam;
import com.github.walkvoid.zone.common.model.UserRoleAssignParam;
import com.github.walkvoid.zone.user.business.db.dao.RoleDAO;
import com.github.walkvoid.zone.user.business.db.dao.RoleMenuRelDAO;
import com.github.walkvoid.zone.user.business.db.dao.UserRoleRelDAO;
import com.github.walkvoid.zone.user.model.dto.RoleDTO;
import com.github.walkvoid.zone.user.model.entity.Role;
import com.github.walkvoid.zone.user.model.entity.RoleMenuRel;
import com.github.walkvoid.zone.user.model.entity.UserRoleRel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 角色管理 — CRUD + 用户角色关联 + 角色菜单关联
 *
 * @author walkvoid
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private RoleDAO roleDAO;
    @Autowired
    private UserRoleRelDAO userRoleRelDAO;
    @Autowired
    private RoleMenuRelDAO roleMenuRelDAO;

    // ==================== 角色 CRUD ====================

    @Operation(summary = "新增角色")
    @PostMapping
    public WebResponse<RoleDTO> add(@RequestBody RoleDTO dto) {
        if (Objects.isNull(dto)) return WebResponse.ok(null);
        Role entity = BeanCopyUtils.copyBean(dto, Role.class);
        roleDAO.insert(entity);
        return WebResponse.ok(BeanCopyUtils.copyBean(entity, RoleDTO.class));
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public WebResponse<Boolean> delete(@Parameter(description = "角色ID") @PathVariable Long id) {
        return WebResponse.ok(!Objects.isNull(id) && roleDAO.deleteById(id) > 0);
    }

    @Operation(summary = "批量删除角色")
    @DeleteMapping("/batch")
    public WebResponse<Boolean> deleteBatch(@RequestBody IdsParam param) {
        if (Objects.isNull(param) || Objects.isNull(param.getIds()) || param.getIds().isEmpty())
            return WebResponse.ok(false);
        return WebResponse.ok(roleDAO.deleteBatchIds(param.getIds()) > 0);
    }

    @Operation(summary = "更新角色")
    @PutMapping
    public WebResponse<RoleDTO> update(@RequestBody RoleDTO dto) {
        if (Objects.isNull(dto) || Objects.isNull(dto.getId())) return WebResponse.ok(null);
        Role entity = BeanCopyUtils.copyBean(dto, Role.class);
        roleDAO.updateById(entity);
        return WebResponse.ok(BeanCopyUtils.copyBean(roleDAO.selectById(dto.getId()), RoleDTO.class));
    }

    @Operation(summary = "查询角色")
    @GetMapping("/{id}")
    public WebResponse<RoleDTO> getById(@Parameter(description = "角色ID") @PathVariable Long id) {
        return WebResponse.ok(BeanCopyUtils.copyBean(roleDAO.selectById(id), RoleDTO.class));
    }

    @Operation(summary = "条件查询角色列表")
    @GetMapping("/list")
    public WebResponse<List<RoleDTO>> list(RoleDTO dto) {
        Role condition = BeanCopyUtils.copyBean(dto, Role.class);
        return WebResponse.ok(roleDAO.selectList(condition).stream()
                .map(r -> BeanCopyUtils.copyBean(r, RoleDTO.class))
                .collect(Collectors.toList()));
    }

    @Operation(summary = "查询所有角色")
    @GetMapping("/all")
    public WebResponse<List<RoleDTO>> all() {
        return WebResponse.ok(roleDAO.selectAll().stream()
                .map(r -> BeanCopyUtils.copyBean(r, RoleDTO.class))
                .collect(Collectors.toList()));
    }

    // ==================== 用户-角色关联 ====================

    @Operation(summary = "为用户分配角色")
    @PostMapping("/user/{userId}/assign")
    public WebResponse<Boolean> assignUserRoles(@Parameter(description = "用户ID") @PathVariable Long userId,
                                    @RequestBody UserRoleAssignParam param) {
        if (Objects.isNull(userId) || Objects.isNull(param) || Objects.isNull(param.getRoleIds()) || param.getRoleIds().isEmpty())
            return WebResponse.ok(false);
        for (Long roleId : param.getRoleIds()) {
            UserRoleRel rel = new UserRoleRel();
            rel.setUserId(userId);
            rel.setRoleId(roleId);
            userRoleRelDAO.insert(rel);
        }
        return WebResponse.ok(true);
    }

    @Operation(summary = "移除用户的指定角色")
    @DeleteMapping("/user/{userId}/role/{roleId}")
    public WebResponse<Boolean> removeUserRole(@Parameter(description = "用户ID") @PathVariable Long userId,
                                   @Parameter(description = "角色ID") @PathVariable Long roleId) {
        return WebResponse.ok(userRoleRelDAO.deleteByUserIdAndRoleId(userId, roleId) > 0);
    }

    @Operation(summary = "移除用户的所有角色")
    @DeleteMapping("/user/{userId}/roles")
    public WebResponse<Boolean> removeUserAllRoles(@Parameter(description = "用户ID") @PathVariable Long userId) {
        return WebResponse.ok(userRoleRelDAO.deleteByUserId(userId) > 0);
    }

    @Operation(summary = "查询用户拥有的角色")
    @GetMapping("/user/{userId}/roles")
    public WebResponse<List<RoleDTO>> getUserRoles(@Parameter(description = "用户ID") @PathVariable Long userId) {
        return WebResponse.ok(userRoleRelDAO.selectByUserId(userId).stream()
                .map(rel -> roleDAO.selectById(rel.getRoleId()))
                .filter(Objects::nonNull)
                .map(r -> BeanCopyUtils.copyBean(r, RoleDTO.class))
                .collect(Collectors.toList()));
    }

    @Operation(summary = "查询拥有该角色的用户ID列表")
    @GetMapping("/{roleId}/users")
    public WebResponse<List<Long>> getRoleUserIds(@Parameter(description = "角色ID") @PathVariable Long roleId) {
        return WebResponse.ok(userRoleRelDAO.selectByRoleId(roleId).stream()
                .map(UserRoleRel::getUserId)
                .collect(Collectors.toList()));
    }

    // ==================== 角色-菜单关联 ====================

    @Operation(summary = "为角色分配菜单")
    @PostMapping("/{roleId}/menus/assign")
    public WebResponse<Boolean> assignRoleMenus(@Parameter(description = "角色ID") @PathVariable Long roleId,
                                    @RequestBody RoleMenuAssignParam param) {
        if (Objects.isNull(roleId) || Objects.isNull(param) || Objects.isNull(param.getMenuIds()) || param.getMenuIds().isEmpty())
            return WebResponse.ok(false);
        roleMenuRelDAO.deleteByRoleId(roleId);
        for (Long menuId : param.getMenuIds()) {
            RoleMenuRel rel = new RoleMenuRel();
            rel.setRoleId(roleId);
            rel.setMenuId(menuId);
            roleMenuRelDAO.insert(rel);
        }
        return WebResponse.ok(true);
    }

    @Operation(summary = "移除角色的指定菜单")
    @DeleteMapping("/{roleId}/menu/{menuId}")
    public WebResponse<Boolean> removeRoleMenu(@Parameter(description = "角色ID") @PathVariable Long roleId,
                                   @Parameter(description = "菜单ID") @PathVariable Long menuId) {
        return WebResponse.ok(roleMenuRelDAO.deleteByRoleIdAndMenuId(roleId, menuId) > 0);
    }

    @Operation(summary = "查询角色拥有的菜单ID列表")
    @GetMapping("/{roleId}/menus")
    public WebResponse<List<Long>> getRoleMenuIds(@Parameter(description = "角色ID") @PathVariable Long roleId) {
        return WebResponse.ok(roleMenuRelDAO.selectMenuIdsByRoleId(roleId));
    }
}
