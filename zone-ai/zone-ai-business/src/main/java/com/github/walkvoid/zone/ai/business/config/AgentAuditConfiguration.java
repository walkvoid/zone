package com.github.walkvoid.zone.ai.business.config;

import com.github.walkvoid.zone.ai.business.agent.AgentAuditProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(AgentAuditProperties.class)
public class AgentAuditConfiguration {
}
