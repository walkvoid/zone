package com.github.walkvoid.zone.ai.business.config;

import com.github.walkvoid.zone.ai.business.tool.sql.SqlQueryToolProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SqlQueryToolProperties.class)
public class SqlQueryToolConfiguration {
}
