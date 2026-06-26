package com.github.walkvoid.zone.system.business.controller;

import com.github.walkvoid.wvframework.models.WebResponse;
import com.github.walkvoid.wvframework.utils.BeanCopyUtils;
import com.github.walkvoid.wvframework.utils.JwtUtils;
import com.github.walkvoid.wvframework.models.BooleanEnum;
import com.github.walkvoid.zone.user.model.enums.UserInfoStatusEnum;
import com.github.walkvoid.zone.system.business.db.dao.MenuDAO;
import com.github.walkvoid.zone.system.model.entity.Menu;
import com.github.walkvoid.zone.user.business.db.dao.RoleDAO;
import com.github.walkvoid.zone.user.business.db.dao.RoleMenuRelDAO;
import com.github.walkvoid.zone.user.business.db.dao.UserInfoDAO;
import com.github.walkvoid.zone.user.business.db.dao.UserRoleRelDAO;
import com.github.walkvoid.zone.user.model.dto.UserInfoDTO;
import com.github.walkvoid.zone.user.model.entity.UserInfo;
import com.github.walkvoid.zone.user.model.entity.UserRoleRel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    @Autowired
    private UserInfoDAO userInfoDAO;
    @Autowired
    private UserRoleRelDAO userRoleRelDAO;
    @Autowired
    private RoleDAO roleDAO;
    @Autowired
    private RoleMenuRelDAO roleMenuRelDAO;
    @Autowired
    private MenuDAO menuDAO;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==================== 注册 ====================

    @Operation(summary = "用户注册")
    @PostMapping("/auth/register")
    public WebResponse<Map<String, String>> register(@RequestBody RegisterRequest req,
                                                      HttpServletResponse response) {
        // 参数校验
        if (req.username == null || req.username.trim().length() < 3 || req.username.trim().length() > 20) {
            return WebResponse.of(400, "用户名需 3-20 个字符", null, "warn");
        }
        if (req.password == null || req.password.length() < 6) {
            return WebResponse.of(400, "密码至少 6 位", null, "warn");
        }

        // 唯一性校验
        if (userInfoDAO.checkUsernameExists(req.username) > 0) {
            return WebResponse.of(400, "用户名已存在", null, "warn");
        }

        // 构建用户
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

        userInfoDAO.insert(user);

        // 分配默认角色 ROLE_USER(id=2)
        UserRoleRel rel = new UserRoleRel();
        rel.setUserId(user.getId());
        rel.setRoleId(2L);
        userRoleRelDAO.insert(rel);

        // 自动登录：生成 token
        List<String> roleCodes = List.of("ROLE_USER");
        String accessToken = JwtUtils.generateAccessToken(user.getId(), user.getUsername(), roleCodes);
        String refreshToken = JwtUtils.generateRefreshToken(user.getId(), user.getUsername());

        Cookie cookie = new Cookie("jwt", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(30 * 24 * 60 * 60);
        response.addCookie(cookie);

        Map<String, String> data = Map.of("accessToken", accessToken);
        return WebResponse.ok(data);
    }

    // ==================== 登录 ====================

    @Operation(summary = "用户登录")
    @PostMapping("/auth/login")
    public WebResponse<Map<String, String>> login(@RequestBody LoginRequest req,
                                                   HttpServletResponse response) {
        // 查用户
        UserInfo user = userInfoDAO.selectByUsername(req.username);
        if (user == null) {
            return WebResponse.of(401, "用户名或密码错误", null, "error");
        }

        // 验密码
        if (!passwordEncoder.matches(req.password, user.getPassword())) {
            return WebResponse.of(401, "用户名或密码错误", null, "error");
        }

        // 获取角色编码列表
        List<String> roleCodes = getUserRoleCodes(user.getId());

        // 生成 token
        String accessToken = JwtUtils.generateAccessToken(user.getId(), user.getUsername(), roleCodes);
        String refreshToken = JwtUtils.generateRefreshToken(user.getId(), user.getUsername());

        // refreshToken 写入 httpOnly cookie
        Cookie cookie = new Cookie("jwt", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
        response.addCookie(cookie);

        // 更新最后登录
        userInfoDAO.updateLastLoginInfo(user.getId(), LocalDateTime.now(), getClientIp());

        Map<String, String> data = Map.of("accessToken", accessToken);
        return WebResponse.ok(data);
    }

    // ==================== 刷新 Token ====================

    @Operation(summary = "刷新 accessToken")
    @PostMapping("/auth/refresh")
    public WebResponse<String> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getCookieValue(request, "jwt");
        if (refreshToken == null) {
            return WebResponse.of(401, "未登录", null, "error");
        }

        var claims = JwtUtils.parseRefreshToken(refreshToken);
        if (claims == null) {
            clearCookie(response);
            return WebResponse.of(401, "登录已过期", null, "error");
        }

        Long userId = JwtUtils.getUserId(claims);
        String username = JwtUtils.getUsername(claims);
        List<String> roleCodes = getUserRoleCodes(userId);

        String newAccessToken = JwtUtils.generateAccessToken(userId, username, roleCodes);
        String newRefreshToken = JwtUtils.generateRefreshToken(userId, username);

        Cookie cookie = new Cookie("jwt", newRefreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(30 * 24 * 60 * 60);
        response.addCookie(cookie);

        return WebResponse.ok(newAccessToken);
    }

    // ==================== 退出 ====================

    @Operation(summary = "退出登录")
    @PostMapping("/auth/logout")
    public WebResponse<Void> logout(HttpServletResponse response) {
        clearCookie(response);
        return WebResponse.ok(null);
    }

    // ==================== 权限码 ====================

    @Operation(summary = "获取当前用户权限码")
    @GetMapping("/auth/codes")
    public WebResponse<List<String>> getCodes() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return WebResponse.of(401, "未登录", null, "error");
        }

        String username = auth.getName();
        UserInfo user = userInfoDAO.selectByUsername(username);
        if (user == null) {
            return WebResponse.ok(List.of());
        }

        // 获取用户角色 → 角色菜单关联 → 菜单权限码
        List<Long> roleIds = userRoleRelDAO.selectByUserId(user.getId()).stream()
                .map(UserRoleRel::getRoleId)
                .toList();

        Set<Long> menuIdSet = new HashSet<>();
        for (Long roleId : roleIds) {
            roleMenuRelDAO.selectByRoleId(roleId).stream()
                    .map(com.github.walkvoid.zone.user.model.entity.RoleMenuRel::getMenuId)
                    .forEach(menuIdSet::add);
        }

        List<String> codes = menuDAO.selectBatchIds(new ArrayList<>(menuIdSet)).stream()
                .map(Menu::getPermission)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        return WebResponse.ok(codes);
    }

    // ==================== 用户信息 ====================

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/user/info")
    public WebResponse<UserInfoDTO> getUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return WebResponse.of(401, "未登录", null, "error");
        }

        String username = auth.getName();
        UserInfo user = userInfoDAO.selectByUsername(username);
        if (user == null) {
            return WebResponse.of(401, "用户不存在", null, "error");
        }

        UserInfoDTO dto = BeanCopyUtils.copyBean(user, UserInfoDTO.class);
        // 清除敏感信息
        dto.setPassword(null);
        return WebResponse.ok(dto);
    }

    // ==================== 辅助方法 ====================

    private List<String> getUserRoleCodes(Long userId) {
        if (userId == null) return List.of();
        return userRoleRelDAO.selectByUserId(userId).stream()
                .map(rel -> roleDAO.selectById(rel.getRoleId()))
                .filter(Objects::nonNull)
                .filter(r -> BooleanEnum.YES.equals(r.getIsSystem())
                        || true) // 所有启用角色都返回
                .map(com.github.walkvoid.zone.user.model.entity.Role::getRoleCode)
                .filter(Objects::nonNull)
                .toList();
    }

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
        return "127.0.0.1"; // TODO: 从 request 获取真实 IP
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
