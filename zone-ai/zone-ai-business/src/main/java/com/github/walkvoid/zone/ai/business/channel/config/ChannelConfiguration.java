package com.github.walkvoid.zone.ai.business.channel.config;

import com.github.walkvoid.zone.ai.business.channel.core.ChannelBotClient;
import com.github.walkvoid.zone.ai.business.channel.core.ChannelMessageHandler;
import com.github.walkvoid.zone.ai.business.channel.core.ChannelProperties;
import com.github.walkvoid.zone.ai.business.channel.support.EchoChannelMessageHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 通道模块装配。
 * <p>
 * 各厂商 {@link com.github.walkvoid.zone.ai.business.channel.core.AbstractChannelBotLifecycle}
 * 实现类通过组件扫描注册，并由 Spring {@code SmartLifecycle} 驱动启停。
 */
@Configuration
@EnableConfigurationProperties(ChannelProperties.class)
public class ChannelConfiguration {

    @Bean
    @ConditionalOnMissingBean(ChannelMessageHandler.class)
    public ChannelMessageHandler channelMessageHandler() {
        return new EchoChannelMessageHandler();
    }

    /**
     * 便于运维查看当前已注册的通道客户端。
     */
    @Bean
    public ChannelRegistry channelRegistry(List<ChannelBotClient> clients) {
        return new ChannelRegistry(clients);
    }

    public static final class ChannelRegistry {
        private final List<ChannelBotClient> clients;

        public ChannelRegistry(List<ChannelBotClient> clients) {
            this.clients = List.copyOf(clients);
        }

        public List<ChannelBotClient> getClients() {
            return clients;
        }
    }
}
