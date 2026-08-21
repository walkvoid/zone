package com.github.walkvoid.zone.ai.channel.core;

/**
 * 通道消息业务处理 SPI。
 * <p>
 * 默认实现可做 echo；后续可替换为调用 ChatClient + Tools 的 Agent 编排。
 */
public interface ChannelMessageHandler {

    /**
     * 处理用户消息。
     */
    void onMessage(ChannelInboundMessage message, ChannelReplySink replySink);

    /**
     * 处理进入会话等事件。默认空实现。
     */
    default void onEvent(ChannelType channelType, String eventType, String requestId,
                         ChannelReplySink replySink) {
        // no-op
    }
}
