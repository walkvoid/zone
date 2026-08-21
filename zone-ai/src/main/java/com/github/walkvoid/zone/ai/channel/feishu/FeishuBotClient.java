package com.github.walkvoid.zone.ai.channel.feishu;

import com.github.walkvoid.zone.ai.channel.core.*;
import com.github.walkvoid.zone.ai.channel.core.ChannelType;
import org.springframework.stereotype.Component;

/**
 * 飞书机器人通道占位实现。
 * <p>
 * 已接入 {@link AbstractChannelBotLifecycle}，后续只需补齐鉴权、长连接与消息编解码，
 * 并将入站事件映射为 {@link ChannelInboundMessage}，
 * 即可复用同一套 {@link ChannelMessageHandler}。
 * <p>
 * 当前 {@link #isAutoStartup()} 固定为 false，避免未实现时误启动。
 */
@Component
public class FeishuBotClient extends AbstractChannelBotLifecycle {

    private final ChannelProperties channelProperties;

    public FeishuBotClient(ChannelProperties channelProperties) {
        this.channelProperties = channelProperties;
    }

    @Override
    public ChannelType channelType() {
        return ChannelType.FEISHU;
    }

    @Override
    public boolean isEnabled() {
        FeishuBotProperties p = channelProperties.getFeishu();
        return channelProperties.isEnabled() && p.isEnabled();
    }

    @Override
    public boolean isAutoStartup() {
        return false;
    }

    @Override
    protected void doStart() {
        FeishuBotProperties props = channelProperties.getFeishu();
        log.warn("[{}] not implemented yet, skip real connect. appId={}, wsUrl={}",
                channelType(), props.getAppId(), props.getWsUrl());
        // TODO:
        // 1) 按飞书开放平台文档完成鉴权与长连接/事件订阅
        // 2) 心跳与断线重连（可参考 WeiXinAiBotClient）
        // 3) 映射为 ChannelInboundMessage，调用 ChannelMessageHandler
        markNotRunning(ChannelConnectionState.STOPPED);
    }

    @Override
    protected void doStop() {
        // no-op until implemented
    }
}
