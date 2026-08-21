package com.github.walkvoid.zone.auth.controller;

import com.github.walkvoid.wvframework.models.ApiResult;
import com.github.walkvoid.wvframework.models.BooleanEnum;
import com.github.walkvoid.wvframework.utils.JwtUtils;
import com.github.walkvoid.zone.auth.service.UserCredentialService;
import com.github.walkvoid.zone.auth.service.UserIdentityService;
import com.github.walkvoid.zone.auth.service.AuthLoginLogService;
import com.github.walkvoid.zone.auth.service.AuthSessionService;
import com.github.walkvoid.zone.auth.util.RequestUtils;
import com.github.walkvoid.zone.auth.model.enums.IdentityTypeEnum;
import com.github.walkvoid.zone.auth.model.enums.LoginTypeEnum;
import com.github.walkvoid.zone.user.client.RoleFeignClient;
import com.github.walkvoid.zone.user.client.UserInfoFeignClient;
import com.github.walkvoid.zone.user.db.entity.UserInfo;
import com.github.walkvoid.zone.user.model.enums.UserInfoStatusEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 认证接口 — 登录、注册、Token 刷新与退出
 */
@Tag(name = "认证管理")
@RestController
public class AuthController {

    @Autowired
    private UserInfoFeignClient userInfoService;
    @Autowired
    private RoleFeignClient roleService;

    @Autowired
    private UserCredentialService userCredentialService;
    @Autowired
    private UserIdentityService userIdentityService;
    @Autowired
    private AuthSessionService authSessionService;
    @Autowired
    private AuthLoginLogService authLoginLogService;

    @Operation(summary = "用户注册")
    @PostMapping("/auth/register")
    public ApiResult<Map<String, String>> register(@RequestBody RegisterRequest req,
                                                   HttpServletRequest request,
                                                   HttpServletResponse response) {
        String clientIp = RequestUtils.getClientIp(request);
        String userAgent = RequestUtils.getUserAgent(request);

        if (req.username == null || req.username.trim().length() < 3 || req.username.trim().length() > 20) {
            return ApiResult.error(400, "用户名需 3-20 个字符");
        }
        if (req.password == null || req.password.length() < 6) {
            return ApiResult.error(400, "密码至少 6 位");
        }
        if (userInfoService.checkUsernameExists(req.username)) {
            authLoginLogService.logFailure(req.username, LoginTypeEnum.REGISTER, "用户名已存在", clientIp, userAgent);
            return ApiResult.error(400, "用户名已存在");
        }

        UserInfo user = new UserInfo();
        user.setUsername(req.username.trim());
        user.setNickname(req.nickname != null ? req.nickname : req.username);
        user.setPhone(req.phone);
        user.setEmail(req.email);
        user.setStatus(UserInfoStatusEnum.ACTIVE);
        user.setIsAdmin(BooleanEnum.NO);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userInfoService.insert(user);

        userIdentityService.createIdentity(user.getId(), IdentityTypeEnum.USERNAME, user.getUsername(), true);
        userCredentialService.createPassword(user.getId(), req.password);

        List<String> roleCodes = List.of("ROLE_USER");
        AuthSessionService.TokenPair tokens = authSessionService.issueTokenPair(
                user.getId(), user.getUsername(), roleCodes, clientIp, userAgent);

        addRefreshCookie(response, tokens.refreshToken());
        authLoginLogService.logSuccess(user.getId(), user.getUsername(), LoginTypeEnum.REGISTER, clientIp, userAgent);

        return ApiResult.ok(Map.of("accessToken", tokens.accessToken()));
    }

    @Operation(summary = "用户登录")
    @PostMapping("/auth/login")
    public ApiResult<Map<String, String>> login(@RequestBody LoginRequest req,
                                                HttpServletRequest request,
                                                HttpServletResponse response) {
        String clientIp = RequestUtils.getClientIp(request);
        String userAgent = RequestUtils.getUserAgent(request);
        String username = req.username;

        Long userId = userIdentityService.findUserIdByUsername(username);
        UserInfo user = userId != null ? userInfoService.getById(userId) : userInfoService.getByUsername(username);
        if (user == null) {
            authLoginLogService.logFailure(username, LoginTypeEnum.PASSWORD, "用户不存在", clientIp, userAgent);
            return ApiResult.error(401, "用户名或密码错误");
        }
        if (UserInfoStatusEnum.DISABLE.equals(user.getStatus())) {
            authLoginLogService.logFailure(username, LoginTypeEnum.PASSWORD, "账号已禁用", clientIp, userAgent);
            return ApiResult.error(403, "账号已禁用");
        }
        if (!userCredentialService.verifyPassword(user.getId(), req.password)) {
            authLoginLogService.logFailure(username, LoginTypeEnum.PASSWORD, "密码错误", clientIp, userAgent);
            return ApiResult.error(401, "用户名或密码错误");
        }

        List<String> roleCodes = roleService.getRoleCodesByUserId(user.getId());
        AuthSessionService.TokenPair tokens = authSessionService.issueTokenPair(
                user.getId(), user.getUsername(), roleCodes, clientIp, userAgent);

        addRefreshCookie(response, tokens.refreshToken());
        userInfoService.updateLastLoginInfo(user.getId(), LocalDateTime.now(), clientIp);
        authLoginLogService.logSuccess(user.getId(), user.getUsername(), LoginTypeEnum.PASSWORD, clientIp, userAgent);

        return ApiResult.ok(Map.of("accessToken", tokens.accessToken()));
    }

    @Operation(summary = "刷新 accessToken")
    @PostMapping("/auth/refresh")
    public ApiResult<String> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getCookieValue(request, "jwt");
        if (refreshToken == null) {
            return ApiResult.error(401, "未登录");
        }

        String clientIp = RequestUtils.getClientIp(request);
        String userAgent = RequestUtils.getUserAgent(request);

        var claims = JwtUtils.parseRefreshToken(refreshToken);
        if (claims == null) {
            clearCookie(response);
            return ApiResult.error(401, "登录已过期");
        }

        AuthSessionService.TokenPair rotated = authSessionService.rotateRefreshToken(refreshToken, clientIp, userAgent);
        if (rotated == null) {
            clearCookie(response);
            return ApiResult.error(401, "登录已过期");
        }

        Long userId = JwtUtils.getUserId(claims);
        String username = JwtUtils.getUsername(claims);
        List<String> roleCodes = roleService.getRoleCodesByUserId(userId);
        String newAccessToken = JwtUtils.generateAccessToken(userId, username, roleCodes);

        addRefreshCookie(response, rotated.refreshToken());

        return ApiResult.ok(newAccessToken);
    }

    @Operation(summary = "退出登录")
    @PostMapping("/auth/logout")
    public ApiResult<String> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getCookieValue(request, "jwt");
        authSessionService.revokeRefreshToken(refreshToken);
        clearCookie(response);
        return ApiResult.ok("OK");
    }

    private void addRefreshCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("jwt", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(30 * 24 * 60 * 60);
        response.addCookie(cookie);
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
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
