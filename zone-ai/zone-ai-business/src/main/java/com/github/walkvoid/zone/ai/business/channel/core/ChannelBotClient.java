package com.github.walkvoid.zone.ai.business.channel.core;

/**
 * 通道机器人客户端抽象。各厂商实现连接、收发与保活。
 */
public interface ChannelBotClient {

    ChannelType channelType();

    ChannelConnectionState connectionState();

    /**
     * 是否已启用（配置开关）。
     */
    boolean isEnabled();
}
