package com.github.walkvoid.zone.ai.business.config;

import com.github.walkvoid.zone.ai.business.tool.repo.RepoToolProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RepoToolProperties.class)
public class RepoToolConfiguration {
}
