package com.github.walkvoid.zone.ai.business.config;

import com.github.walkvoid.zone.ai.business.agent.AgentMemoryProperties;
import com.github.walkvoid.zone.ai.business.agent.GroupChatMemoryService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AgentMemoryProperties.class)
public class AgentChatMemoryConfiguration {

    @Bean
    public GroupChatMemoryService groupChatMemoryService(AgentMemoryProperties properties) {
        return new GroupChatMemoryService(properties);
    }
}
