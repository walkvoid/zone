package com.github.walkvoid.zone.ai.business.config;

import com.github.walkvoid.zone.ai.business.tool.log.BeeCloudProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(BeeCloudProperties.class)
public class BeeCloudToolConfiguration {
}
