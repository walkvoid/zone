package com.github.walkvoid.zone.ai.config;

import com.github.walkvoid.zone.ai.tool.sql.SqlQueryToolProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SqlQueryToolProperties.class)
public class SqlQueryToolConfiguration {
}
