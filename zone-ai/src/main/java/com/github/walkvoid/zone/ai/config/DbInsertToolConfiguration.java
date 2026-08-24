package com.github.walkvoid.zone.ai.config;

import com.github.walkvoid.zone.ai.tool.write.DbInsertToolProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DbInsertToolProperties.class)
public class DbInsertToolConfiguration {
}
