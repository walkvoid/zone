package com.github.walkvoid.zone.auth.business.service;

import java.util.List;

/**
 * Refresh Token 会话管理
 */
public interface AuthSessionService {

    /**
     * 签发 Refresh Token 并落库
     */
    String issueRefreshToken(Long userId, String username, String clientIp, String userAgent);

    /**
     * 校验并轮换 Refresh Token，返回新的 access/refresh；失败返回 null
     */
    TokenPair rotateRefreshToken(String refreshToken, String clientIp, String userAgent);

    /**
     * 撤销当前 Refresh Token
     */
    void revokeRefreshToken(String refreshToken);

    /**
     * 撤销用户全部活跃会话（踢人）
     */
    void revokeAllByUserId(Long userId);

    record TokenPair(String accessToken, String refreshToken) {
    }

    /**
     * 签发 access + refresh
     */
    TokenPair issueTokenPair(Long userId, String username, List<String> roleCodes,
                             String clientIp, String userAgent);
}
