package com.github.walkvoid.zone.auth.controller;

import com.github.walkvoid.wvframework.models.ApiResult;
import com.github.walkvoid.wvframework.models.BooleanEnum;
import com.github.walkvoid.wvframework.core.jwt.JwtSupport;
import com.github.walkvoid.zone.auth.client.SmsSendRecordFeignClient;
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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    @Autowired
    private JwtSupport jwtSupport;
    @Autowired
    private SmsSendRecordFeignClient smsSendRecordFeignClient;

    private static final String PHONE_PATTERN = "^1[3-9]\\d{9}$";
    private static final String SMS_BIZ_LOGIN = "LOGIN";

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
        UserInfo created = userInfoService.getByUsername(user.getUsername());
        if (created == null || created.getId() == null) {
            return ApiResult.error(500, "注册失败");
        }
        user = created;

        userIdentityService.createIdentity(user.getId(), IdentityTypeEnum.USERNAME, user.getUsername(), true);
        if (StringUtils.hasText(user.getPhone())) {
            userIdentityService.createIdentity(user.getId(), IdentityTypeEnum.PHONE, user.getPhone().trim(), true);
        }
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

    @Operation(summary = "发送登录验证码")
    @PostMapping("/auth/sms/send")
    public ApiResult<Void> sendSmsCode(@RequestBody SmsSendRequest req, HttpServletRequest request) {
        String phone = req == null ? null : req.phone;
        if (!isValidPhone(phone)) {
            return ApiResult.error(400, "手机号格式不正确");
        }
        String clientIp = RequestUtils.getClientIp(request);
        ApiResult<Void> result = smsSendRecordFeignClient.send(phone.trim(), SMS_BIZ_LOGIN, clientIp);
        if (result == null) {
            return ApiResult.error(500, "短信服务不可用");
        }
        return result;
    }

    @Operation(summary = "手机验证码登录（未注册自动注册）")
    @PostMapping("/auth/sms/login")
    public ApiResult<Map<String, String>> smsLogin(@RequestBody SmsLoginRequest req,
                                                   HttpServletRequest request,
                                                   HttpServletResponse response) {
        String clientIp = RequestUtils.getClientIp(request);
        String userAgent = RequestUtils.getUserAgent(request);
        String phone = req == null ? null : req.phone;
        String code = req == null ? null : req.code;

        if (!isValidPhone(phone)) {
            return ApiResult.error(400, "手机号格式不正确");
        }
        if (!StringUtils.hasText(code)) {
            return ApiResult.error(400, "验证码不能为空");
        }

        ApiResult<Boolean> verifyResult = smsSendRecordFeignClient.verify(phone.trim(), SMS_BIZ_LOGIN, code.trim());
        if (verifyResult == null || !isSuccess(verifyResult) || !Boolean.TRUE.equals(verifyResult.getData())) {
            String msg = verifyResult != null && StringUtils.hasText(verifyResult.getMsg())
                    ? verifyResult.getMsg() : "验证码错误";
            authLoginLogService.logFailure(phone, LoginTypeEnum.SMS, msg, clientIp, userAgent);
            return ApiResult.error(401, msg);
        }

        Long userId = userIdentityService.findUserIdByPhone(phone.trim());
        UserInfo user = userId != null ? userInfoService.getById(userId) : null;
        boolean registered = false;
        if (user == null) {
            user = createUserByPhone(phone.trim());
            registered = true;
        }
        if (UserInfoStatusEnum.DISABLE.equals(user.getStatus())) {
            authLoginLogService.logFailure(phone, LoginTypeEnum.SMS, "账号已禁用", clientIp, userAgent);
            return ApiResult.error(403, "账号已禁用");
        }

        List<String> roleCodes = roleService.getRoleCodesByUserId(user.getId());
        if (roleCodes == null || roleCodes.isEmpty()) {
            roleCodes = List.of("ROLE_USER");
        }
        AuthSessionService.TokenPair tokens = authSessionService.issueTokenPair(
                user.getId(), user.getUsername(), roleCodes, clientIp, userAgent);

        addRefreshCookie(response, tokens.refreshToken());
        userInfoService.updateLastLoginInfo(user.getId(), LocalDateTime.now(), clientIp);
        authLoginLogService.logSuccess(user.getId(), user.getUsername(),
                registered ? LoginTypeEnum.REGISTER : LoginTypeEnum.SMS, clientIp, userAgent);

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

        var claims = jwtSupport.parseRefreshToken(refreshToken);
        if (claims == null) {
            clearCookie(response);
            return ApiResult.error(401, "登录已过期");
        }

        AuthSessionService.TokenPair rotated = authSessionService.rotateRefreshToken(refreshToken, clientIp, userAgent);
        if (rotated == null) {
            clearCookie(response);
            return ApiResult.error(401, "登录已过期");
        }

        Long userId = jwtSupport.getUserId(claims);
        String username = jwtSupport.getUsername(claims);
        List<String> roleCodes = roleService.getRoleCodesByUserId(userId);
        String newAccessToken = jwtSupport.generateAccessToken(userId, username, roleCodes);

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

    private UserInfo createUserByPhone(String phone) {
        UserInfo user = new UserInfo();
        user.setUsername(phone);
        user.setNickname(maskPhone(phone));
        user.setPhone(phone);
        user.setStatus(UserInfoStatusEnum.ACTIVE);
        user.setIsAdmin(BooleanEnum.NO);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userInfoService.insert(user);

        // Feign insert 只返回行数，需回查获取主键
        UserInfo created = userInfoService.getByUsername(phone);
        if (created == null || created.getId() == null) {
            throw new IllegalStateException("create user by phone failed");
        }
        userIdentityService.createIdentity(created.getId(), IdentityTypeEnum.PHONE, phone, true);
        userIdentityService.createIdentity(created.getId(), IdentityTypeEnum.USERNAME, phone, true);
        return created;
    }

    private static boolean isValidPhone(String phone) {
        return StringUtils.hasText(phone) && phone.trim().matches(PHONE_PATTERN);
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private static boolean isSuccess(ApiResult<?> result) {
        Serializable code = result.getCode();
        return Objects.equals(code, 0) || Objects.equals(String.valueOf(code), "0");
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

    public static class SmsSendRequest {
        public String phone;
    }

    public static class SmsLoginRequest {
        public String phone;
        public String code;
    }
}
