package com.github.walkvoid.zone.auth.service.impl;

import com.github.walkvoid.wvframework.utils.JwtUtils;
import com.github.walkvoid.zone.auth.config.JwtProperties;
import com.github.walkvoid.zone.auth.db.dao.AuthRefreshTokenDAO;
import com.github.walkvoid.zone.auth.service.AuthSessionService;
import com.github.walkvoid.zone.auth.util.TokenHashUtils;
import com.github.walkvoid.zone.auth.db.entity.AuthRefreshToken;
import com.github.walkvoid.zone.auth.model.enums.SessionStatusEnum;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthSessionServiceImpl implements AuthSessionService {

    @Autowired
    private AuthRefreshTokenDAO authRefreshTokenDAO;
    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public String issueRefreshToken(Long userId, String username, String clientIp, String userAgent) {
        String refreshToken = JwtUtils.generateRefreshToken(userId, username);
        persistSession(userId, refreshToken, clientIp, userAgent);
        return refreshToken;
    }

    @Override
    public TokenPair issueTokenPair(Long userId, String username, List<String> roleCodes,
                                    String clientIp, String userAgent) {
        String accessToken = JwtUtils.generateAccessToken(userId, username, roleCodes);
        String refreshToken = issueRefreshToken(userId, username, clientIp, userAgent);
        return new TokenPair(accessToken, refreshToken);
    }

    @Override
    public TokenPair rotateRefreshToken(String refreshToken, String clientIp, String userAgent) {
        Claims claims = JwtUtils.parseRefreshToken(refreshToken);
        if (claims == null) {
            return null;
        }

        String tokenHash = TokenHashUtils.sha256(refreshToken);
        AuthRefreshToken session = authRefreshTokenDAO.selectActiveByTokenHash(tokenHash);
        if (session == null) {
            return null;
        }

        Long userId = JwtUtils.getUserId(claims);
        String username = JwtUtils.getUsername(claims);
        if (userId == null || !userId.equals(session.getUserId())) {
            return null;
        }

        authRefreshTokenDAO.updateStatus(session.getId(), SessionStatusEnum.ROTATED);

        String newRefreshToken = JwtUtils.generateRefreshToken(userId, username);
        persistSession(userId, newRefreshToken, clientIp, userAgent);

        return new TokenPair(null, newRefreshToken);
    }

    @Override
    public void revokeRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        authRefreshTokenDAO.revokeByTokenHash(TokenHashUtils.sha256(refreshToken));
    }

    @Override
    public void revokeAllByUserId(Long userId) {
        if (userId != null) {
            authRefreshTokenDAO.revokeAllActiveByUserId(userId);
        }
    }

    private void persistSession(Long userId, String refreshToken, String clientIp, String userAgent) {
        AuthRefreshToken session = new AuthRefreshToken();
        session.setUserId(userId);
        session.setTokenHash(TokenHashUtils.sha256(refreshToken));
        session.setStatus(SessionStatusEnum.ACTIVE);
        session.setExpiresAt(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenExpiration() / 1000));
        session.setClientIp(clientIp);
        session.setUserAgent(truncateUserAgent(userAgent));
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        authRefreshTokenDAO.insert(session);
    }

    private String truncateUserAgent(String userAgent) {
        if (userAgent == null) {
            return null;
        }
        return userAgent.length() > 512 ? userAgent.substring(0, 512) : userAgent;
    }
}
