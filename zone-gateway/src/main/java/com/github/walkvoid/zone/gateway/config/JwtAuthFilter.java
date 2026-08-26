package com.github.walkvoid.zone.gateway.config;

import com.github.walkvoid.wvframework.core.jwt.JwtAuthorityConverter;
import com.github.walkvoid.wvframework.core.jwt.JwtSupport;
import com.github.walkvoid.wvframework.core.security.PermissionResolver;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * JWT 认证过滤器 (WebFlux) — 权限按 wv.security.permission-store 解析
 */
@Component
public class JwtAuthFilter implements WebFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtSupport jwtSupport;
    private final PermissionResolver permissionResolver;

    public JwtAuthFilter(JwtSupport jwtSupport, PermissionResolver permissionResolver) {
        this.jwtSupport = jwtSupport;
        this.permissionResolver = permissionResolver;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = extractToken(exchange.getRequest());

        if (token != null) {
            Claims claims = jwtSupport.parseAccessToken(token);
            if (claims != null) {
                String username = jwtSupport.getUsername(claims);
                Long userId = jwtSupport.getUserId(claims);
                return Mono.fromCallable(() -> JwtAuthorityConverter.toAuthorities(
                                jwtSupport.getRoles(claims),
                                permissionResolver.resolve(userId, claims)))
                        .subscribeOn(Schedulers.boundedElastic())
                        .flatMap(authorities -> {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(username, null, authorities);
                            return chain.filter(exchange)
                                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                        });
            }
        }

        return chain.filter(exchange);
    }

    private String extractToken(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
