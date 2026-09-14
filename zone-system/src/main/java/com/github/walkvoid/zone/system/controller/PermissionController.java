package com.github.walkvoid.zone.system.controller;

import com.github.walkvoid.wvframework.models.ApiResult;
import com.github.walkvoid.zone.system.service.PermissionService;
import com.github.walkvoid.zone.user.client.UserInfoClient;
import com.github.walkvoid.zone.user.db.entity.UserInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 授权接口 — 前端按钮权限码
 */
@Tag(name = "权限管理")
@RestController
public class PermissionController {

    @Autowired
    private UserInfoClient userInfoService;
    @Autowired
    private PermissionService permissionService;

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
        return ApiResult.ok(permissionService.listPermissionCodesByUserId(user.getId()));
    }
}
