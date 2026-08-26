package com.github.walkvoid.zone.auth.service.impl;

import com.github.walkvoid.wvframework.core.jwt.JwtSupport;
import com.github.walkvoid.wvframework.core.security.PermissionCache;
import com.github.walkvoid.wvframework.core.security.PermissionSource;
import com.github.walkvoid.wvframework.core.security.WvSecurityProperties;
import com.github.walkvoid.zone.auth.db.dao.AuthRefreshTokenDAO;
import com.github.walkvoid.zone.auth.db.entity.AuthRefreshToken;
import com.github.walkvoid.zone.auth.model.enums.SessionStatusEnum;
import com.github.walkvoid.zone.auth.service.AuthSessionService;
import com.github.walkvoid.zone.auth.util.TokenHashUtils;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthSessionServiceImpl implements AuthSessionService {

    private static final Logger log = LoggerFactory.getLogger(AuthSessionServiceImpl.class);

    @Autowired
    private AuthRefreshTokenDAO authRefreshTokenDAO;
    @Autowired
    private JwtSupport jwtSupport;
    @Autowired
    private WvSecurityProperties securityProperties;
    @Autowired
    private ObjectProvider<PermissionSource> permissionSource;
    @Autowired
    private ObjectProvider<PermissionCache> permissionCache;

    @Override
    public String issueRefreshToken(Long userId, String username, String clientIp, String userAgent) {
        String refreshToken = jwtSupport.generateRefreshToken(userId, username);
        persistSession(userId, refreshToken, clientIp, userAgent);
        return refreshToken;
    }

    @Override
    public TokenPair issueTokenPair(Long userId, String username, List<String> roleCodes,
                                    String clientIp, String userAgent) {
        String accessToken = issueAccessToken(userId, username, roleCodes);
        String refreshToken = issueRefreshToken(userId, username, clientIp, userAgent);
        return new TokenPair(accessToken, refreshToken);
    }

    @Override
    public String issueAccessToken(Long userId, String username, List<String> roleCodes) {
        List<String> permissions = loadPermissions(userId);
        if (securityProperties.isCacheStore()) {
            PermissionCache cache = permissionCache.getIfAvailable();
            if (cache != null) {
                cache.put(userId, permissions);
            } else {
                log.warn("permission-store=cache but PermissionCache unavailable; permissions not cached");
            }
            return jwtSupport.generateAccessToken(userId, username, roleCodes, null);
        }
        return jwtSupport.generateAccessToken(userId, username, roleCodes, permissions);
    }

    @Override
    public TokenPair rotateRefreshToken(String refreshToken, String clientIp, String userAgent) {
        Claims claims = jwtSupport.parseRefreshToken(refreshToken);
        if (claims == null) {
            return null;
        }

        String tokenHash = TokenHashUtils.sha256(refreshToken);
        AuthRefreshToken session = authRefreshTokenDAO.selectActiveByTokenHash(tokenHash);
        if (session == null) {
            return null;
        }

        Long userId = jwtSupport.getUserId(claims);
        String username = jwtSupport.getUsername(claims);
        if (userId == null || !userId.equals(session.getUserId())) {
            return null;
        }

        authRefreshTokenDAO.updateStatus(session.getId(), SessionStatusEnum.ROTATED);

        String newRefreshToken = jwtSupport.generateRefreshToken(userId, username);
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

    private List<String> loadPermissions(Long userId) {
        PermissionSource source = permissionSource.getIfAvailable();
        if (source == null) {
            log.warn("PermissionSource missing, userId={}", userId);
            return List.of();
        }
        try {
            List<String> codes = source.loadByUserId(userId);
            return codes != null ? codes : List.of();
        } catch (Exception e) {
            log.warn("load permissions failed for userId={}: {}", userId, e.getMessage());
            return List.of();
        }
    }

    private void persistSession(Long userId, String refreshToken, String clientIp, String userAgent) {
        AuthRefreshToken session = new AuthRefreshToken();
        session.setUserId(userId);
        session.setTokenHash(TokenHashUtils.sha256(refreshToken));
        session.setStatus(SessionStatusEnum.ACTIVE);
        session.setExpiresAt(LocalDateTime.now().plusSeconds(jwtSupport.getRefreshTokenExpiration() / 1000));
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
