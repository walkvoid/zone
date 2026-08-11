package com.github.walkvoid.zone.system.business.controller;

import com.github.walkvoid.wvframework.models.ApiResult;
import com.github.walkvoid.wvframework.models.BooleanEnum;
import com.github.walkvoid.wvframework.utils.BeanCopyUtils;
import com.github.walkvoid.wvframework.utils.JwtUtils;
import com.github.walkvoid.zone.user.api.service.RoleMenuRelService;
import com.github.walkvoid.zone.user.api.service.RoleService;
import com.github.walkvoid.zone.user.api.service.UserInfoService;
import com.github.walkvoid.zone.user.model.dto.UserInfoDTO;
import com.github.walkvoid.zone.user.model.entity.UserInfo;
import com.github.walkvoid.zone.user.model.enums.UserInfoStatusEnum;
import com.github.walkvoid.zone.system.business.db.dao.MenuDAO;
import com.github.walkvoid.zone.system.model.entity.Menu;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "认证管理")
@RestController
public class AuthController {

    @DubboReference
    private UserInfoService userInfoService;
    @DubboReference
    private RoleService roleService;
    @DubboReference
    private RoleMenuRelService roleMenuRelService;

    @Autowired
    private MenuDAO menuDAO;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==================== 注册 ====================

    @Operation(summary = "用户注册")
    @PostMapping("/auth/register")
    public ApiResult<Map<String, String>> register(@RequestBody RegisterRequest req,
                                                      HttpServletResponse response) {
        if (req.username == null || req.username.trim().length() < 3 || req.username.trim().length() > 20) {
            return ApiResult.error(400, "用户名需 3-20 个字符");
        }
        if (req.password == null || req.password.length() < 6) {
            return ApiResult.error(400, "密码至少 6 位");
        }
        if (userInfoService.checkUsernameExists(req.username)) {
            return ApiResult.error(400, "用户名已存在");
        }

        UserInfo user = new UserInfo();
        user.setUsername(req.username.trim());
        user.setPassword(passwordEncoder.encode(req.password));
        user.setNickname(req.nickname != null ? req.nickname : req.username);
        user.setPhone(req.phone);
        user.setEmail(req.email);
        user.setStatus(UserInfoStatusEnum.ACTIVE);
        user.setIsAdmin(BooleanEnum.NO);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userInfoService.insert(user);

        List<String> roleCodes = List.of("ROLE_USER");
        String accessToken = JwtUtils.generateAccessToken(user.getId(), user.getUsername(), roleCodes);
        String refreshToken = JwtUtils.generateRefreshToken(user.getId(), user.getUsername());

        Cookie cookie = new Cookie("jwt", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(30 * 24 * 60 * 60);
        response.addCookie(cookie);

        Map<String, String> data = Map.of("accessToken", accessToken);
        return ApiResult.ok(data);
    }

    // ==================== 登录 ====================

    @Operation(summary = "用户登录")
    @PostMapping("/auth/login")
    public ApiResult<Map<String, String>> login(@RequestBody LoginRequest req,
                                                   HttpServletResponse response) {
        UserInfo user = userInfoService.getByUsername(req.username);
        if (user == null) {
            return ApiResult.error(401, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(req.password, user.getPassword())) {
            return ApiResult.error(401, "用户名或密码错误");
        }

        List<String> roleCodes = roleService.getRoleCodesByUserId(user.getId());

        String accessToken = JwtUtils.generateAccessToken(user.getId(), user.getUsername(), roleCodes);
        String refreshToken = JwtUtils.generateRefreshToken(user.getId(), user.getUsername());

        Cookie cookie = new Cookie("jwt", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(30 * 24 * 60 * 60);
        response.addCookie(cookie);

        userInfoService.updateLastLoginInfo(user.getId(), LocalDateTime.now(), getClientIp());

        Map<String, String> data = Map.of("accessToken", accessToken);
        return ApiResult.ok(data);
    }

    // ==================== 刷新 Token ====================

    @Operation(summary = "刷新 accessToken")
    @PostMapping("/auth/refresh")
    public ApiResult<String> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getCookieValue(request, "jwt");
        if (refreshToken == null) {
            return ApiResult.error(401, "未登录");
        }

        var claims = JwtUtils.parseRefreshToken(refreshToken);
        if (claims == null) {
            clearCookie(response);
            return ApiResult.error(401, "登录已过期");
        }

        Long userId = JwtUtils.getUserId(claims);
        String username = JwtUtils.getUsername(claims);
        List<String> roleCodes = roleService.getRoleCodesByUserId(userId);

        String newAccessToken = JwtUtils.generateAccessToken(userId, username, roleCodes);
        String newRefreshToken = JwtUtils.generateRefreshToken(userId, username);

        Cookie cookie = new Cookie("jwt", newRefreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(30 * 24 * 60 * 60);
        response.addCookie(cookie);

        return ApiResult.ok(newAccessToken);
    }

    // ==================== 退出 ====================

    @Operation(summary = "退出登录")
    @PostMapping("/auth/logout")
    public ApiResult<String> logout(HttpServletResponse response) {
        clearCookie(response);
        return ApiResult.ok("OK");
    }

    // ==================== 权限码 ====================

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

    // ==================== 用户信息 ====================

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/system/user/info")
    public ApiResult<UserInfoDTO> getUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ApiResult.error(401, "未登录");
        }

        String username = auth.getName();
        UserInfo user = userInfoService.getByUsername(username);
        if (user == null) {
            return ApiResult.error(401, "用户不存在");
        }

        UserInfoDTO dto = BeanCopyUtils.copyBean(user, UserInfoDTO.class);
        dto.setPassword(null);
        return ApiResult.ok(dto);
    }

    // ==================== 辅助方法 ====================

    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies)
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private void clearCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String getClientIp() {
        return "127.0.0.1";
    }

    // ==================== DTO ====================

    public static class RegisterRequest {
        public String username;
        public String password;
        public String nickname;
        public String phone;
        public String email;
    }

    public static class LoginRequest {
        public String username;
        public String password;
    }
}
