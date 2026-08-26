package com.github.walkvoid.zone.system.config;

import com.github.walkvoid.wvframework.core.security.PermissionSource;
import com.github.walkvoid.zone.system.service.PermissionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * system 本地算权，覆盖框架基于 URL 的默认 PermissionSource。
 */
@Configuration
public class PermissionSourceConfig {

    @Bean
    public PermissionSource permissionSource(PermissionService permissionService) {
        return permissionService::listPermissionCodesByUserId;
    }
}
