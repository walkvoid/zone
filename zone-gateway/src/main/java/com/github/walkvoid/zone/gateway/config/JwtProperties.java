package com.github.walkvoid.zone.gateway.config;

import com.github.walkvoid.wvframework.utils.JwtUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性 — 启动时初始化 JwtUtils
 *
 * @author walkvoid
 */
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private long accessTokenExpiration = 7 * 24 * 60 * 60 * 1000L; // 7 days
    private long refreshTokenExpiration = 30 * 24 * 60 * 60 * 1000L; // 30 days

    @PostConstruct
    public void init() {
        JwtUtils.init(secret);
        JwtUtils.setExpiration(accessTokenExpiration, refreshTokenExpiration);
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public void setAccessTokenExpiration(long accessTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public void setRefreshTokenExpiration(long refreshTokenExpiration) {
        this.refreshTokenExpiration = refreshTokenExpiration;
    }
}
