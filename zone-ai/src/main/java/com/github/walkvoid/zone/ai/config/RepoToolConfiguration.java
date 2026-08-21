package com.github.walkvoid.zone.ai.config;

import com.github.walkvoid.zone.ai.tool.repo.RepoToolProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RepoToolProperties.class)
public class RepoToolConfiguration {
}
