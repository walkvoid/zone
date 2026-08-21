package com.github.walkvoid.zone.ai.channel.core;

/**
 * 通道连接状态。
 */
public enum ChannelConnectionState {
    STOPPED,
    STARTING,
    CONNECTING,
    SUBSCRIBING,
    READY,
    RECONNECTING,
    STOPPING,
    FAILED
}
