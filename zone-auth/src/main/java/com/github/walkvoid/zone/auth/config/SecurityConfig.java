package com.github.walkvoid.zone.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Auth 服务特有 Bean；SecurityFilterChain / JWT 过滤器由 core 自动装配，
 * 放行路径见 {@code wv.security.permit-all-paths}。
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
