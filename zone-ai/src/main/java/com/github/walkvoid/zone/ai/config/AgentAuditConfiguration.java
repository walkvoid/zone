package com.github.walkvoid.zone.ai.config;

import com.github.walkvoid.zone.ai.agent.AgentAuditProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(AgentAuditProperties.class)
public class AgentAuditConfiguration {
}
