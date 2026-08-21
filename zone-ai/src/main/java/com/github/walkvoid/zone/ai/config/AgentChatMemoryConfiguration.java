package com.github.walkvoid.zone.ai.config;

import com.github.walkvoid.zone.ai.agent.AgentMemoryProperties;
import com.github.walkvoid.zone.ai.agent.GroupChatMemoryService;
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
