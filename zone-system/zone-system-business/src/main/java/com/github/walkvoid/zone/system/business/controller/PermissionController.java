package com.github.walkvoid.zone.system.business.controller;

import com.github.walkvoid.wvframework.models.ApiResult;
import com.github.walkvoid.zone.system.business.db.dao.MenuDAO;
import com.github.walkvoid.zone.system.model.entity.Menu;
import com.github.walkvoid.zone.user.api.client.RoleFeignClient;
import com.github.walkvoid.zone.user.api.client.RoleMenuRelFeignClient;
import com.github.walkvoid.zone.user.api.client.UserInfoFeignClient;
import com.github.walkvoid.zone.user.model.entity.UserInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 授权接口 — 前端按钮权限码
 */
@Tag(name = "权限管理")
@RestController
public class PermissionController {

    @Autowired
    private UserInfoFeignClient userInfoService;
    @Autowired
    private RoleFeignClient roleService;
    @Autowired
    private RoleMenuRelFeignClient roleMenuRelService;

    @Autowired
    private MenuDAO menuDAO;

    @Operation(summary = "获取当前用户权限码")
    @GetMapping("/auth/codes")
    public ApiResult<List<String>> getCodes() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ApiResult.error(401, "未登录");
        }

        String username = auth.getName();
        UserInfo user = userInfoService.getByUsername(username);
        if (user == null) {
            return ApiResult.ok(List.of());
        }

        Set<Long> menuIdSet = new HashSet<>();
        var roles = roleService.getRoleCodesByUserId(user.getId());

        var allRoles = roleService.selectAll();
        List<Long> roleIds = allRoles.stream()
                .filter(r -> roles.contains(r.getRoleCode()))
                .map(r -> r.getId())
                .toList();

        for (Long roleId : roleIds) {
            roleMenuRelService.selectByRoleId(roleId).stream()
                    .map(com.github.walkvoid.zone.user.model.entity.RoleMenuRel::getMenuId)
                    .forEach(menuIdSet::add);
        }

        List<String> codes = menuDAO.selectBatchIds(new ArrayList<>(menuIdSet)).stream()
                .map(Menu::getPermission)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        return ApiResult.ok(codes);
    }
}
