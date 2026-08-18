package com.github.walkvoid.zone.ai.business.channel.weixin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.walkvoid.zone.ai.business.agent.AiBotConfigService;
import com.github.walkvoid.zone.ai.business.channel.core.AbstractChannelBotLifecycle;
import com.github.walkvoid.zone.ai.business.channel.core.ChannelConnectionState;
import com.github.walkvoid.zone.ai.business.channel.core.ChannelMessageHandler;
import com.github.walkvoid.zone.ai.business.channel.core.ChannelProperties;
import com.github.walkvoid.zone.ai.business.channel.core.ChannelType;
import com.github.walkvoid.zone.ai.model.entity.AiBotConfig;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 企业微信智能机器人长连接管理器：按 {@code ai_bot_config} 为每个 bot 建一条 WebSocket。
 */
@Component
public class WeiXinAiBotClient extends AbstractChannelBotLifecycle {

    private final ChannelProperties channelProperties;
    private final ChannelMessageHandler messageHandler;
    private final ObjectMapper objectMapper;
    private final AiBotConfigService aiBotConfigService;
    private final HttpClient httpClient;
    private final List<WeiXinAiBotSession> sessions = new ArrayList<>();

    public WeiXinAiBotClient(ChannelProperties channelProperties,
                             ChannelMessageHandler messageHandler,
                             ObjectMapper objectMapper,
                             AiBotConfigService aiBotConfigService) {
        this.channelProperties = channelProperties;
        this.messageHandler = messageHandler;
        this.objectMapper = objectMapper;
        this.aiBotConfigService = aiBotConfigService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    @Override
    public ChannelType channelType() {
        return ChannelType.WEIXIN;
    }

    @Override
    public boolean isEnabled() {
        WeiXinAiBotProperties p = channelProperties.getWeixin();
        if (!(channelProperties.isEnabled() && p.isEnabled())) {
            return false;
        }
        return !aiBotConfigService.listEnabledWeixin(p).isEmpty();
    }

    @Override
    public ChannelConnectionState connectionState() {
        if (sessions.isEmpty()) {
            return super.connectionState();
        }
        boolean anyReady = false;
        boolean anyConnecting = false;
        boolean anyFailed = false;
        for (WeiXinAiBotSession session : sessions) {
            ChannelConnectionState state = session.state();
            if (state == ChannelConnectionState.READY) {
                anyReady = true;
            } else if (state == ChannelConnectionState.CONNECTING
                    || state == ChannelConnectionState.SUBSCRIBING
                    || state == ChannelConnectionState.RECONNECTING) {
                anyConnecting = true;
            } else if (state == ChannelConnectionState.FAILED) {
                anyFailed = true;
            }
        }
        if (anyReady) {
            return ChannelConnectionState.READY;
        }
        if (anyConnecting) {
            return ChannelConnectionState.CONNECTING;
        }
        if (anyFailed) {
            return ChannelConnectionState.FAILED;
        }
        return super.connectionState();
    }

    @Override
    protected void doStart() {
        WeiXinAiBotProperties transport = channelProperties.getWeixin();
        List<AiBotConfig> bots = aiBotConfigService.listEnabledWeixin(transport);
        if (bots.isEmpty()) {
            throw new IllegalStateException("No enabled WeiXin aibot in ai_bot_config and properties fallback is empty");
        }
        log.info("WeiXin starting {} bot session(s)", bots.size());
        for (AiBotConfig bot : bots) {
            WeiXinAiBotSession session = new WeiXinAiBotSession(
                    bot.getBotId().trim(),
                    bot.getSecret(),
                    bot.getWelcomeText(),
                    transport,
                    messageHandler,
                    objectMapper,
                    httpClient,
                    this::shouldKeepRunning,
                    () -> updateState(ChannelConnectionState.READY));
            sessions.add(session);
            session.start();
            log.info("WeiXin session started, botCode={}, botId={}", bot.getBotCode(), bot.getBotId());
        }
    }

    @Override
    protected void doStop() {
        for (WeiXinAiBotSession session : sessions) {
            try {
                session.stop();
            } catch (Exception e) {
                log.warn("WeiXin[{}] stop error: {}", session.botId(), e.getMessage());
            }
        }
        sessions.clear();
    }
}
