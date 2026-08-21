package com.github.walkvoid.zone.ai.config;

import com.github.walkvoid.zone.ai.tool.log.BeeCloudProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(BeeCloudProperties.class)
public class BeeCloudToolConfiguration {
}
