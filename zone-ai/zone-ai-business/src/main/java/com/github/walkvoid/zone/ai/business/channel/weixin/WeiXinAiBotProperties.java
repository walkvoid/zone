package com.github.walkvoid.zone.ai.business.channel.weixin;

/**
 * 企业微信智能机器人长连接配置。
 */
public class WeiXinAiBotProperties {

    /**
     * 是否启用企业微信通道（还需 zone.channel.enabled=true）。
     */
    private boolean enabled = false;

    /**
     * WebSocket 网关地址。
     */
    private String wsUrl = "wss://openws.work.weixin.qq.com";

    /**
     * 智能机器人 BotID。
     */
    private String botId = "";

    /**
     * 长连接专用 Secret。
     */
    private String secret = "";

    /**
     * 心跳间隔（毫秒），官方建议 30s。
     */
    private long heartbeatIntervalMs = 30_000L;

    /**
     * 首次重连等待（毫秒）。
     */
    private long reconnectInitialMs = 1_000L;

    /**
     * 重连等待上限（毫秒）。
     */
    private long reconnectMaxMs = 60_000L;

    /**
     * 进入会话欢迎语。
     */
    private String welcomeText = "你好，我是 Zone AI 助手。";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getWsUrl() {
        return wsUrl;
    }

    public void setWsUrl(String wsUrl) {
        this.wsUrl = wsUrl;
    }

    public String getBotId() {
        return botId;
    }

    public void setBotId(String botId) {
        this.botId = botId;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getHeartbeatIntervalMs() {
        return heartbeatIntervalMs;
    }

    public void setHeartbeatIntervalMs(long heartbeatIntervalMs) {
        this.heartbeatIntervalMs = heartbeatIntervalMs;
    }

    public long getReconnectInitialMs() {
        return reconnectInitialMs;
    }

    public void setReconnectInitialMs(long reconnectInitialMs) {
        this.reconnectInitialMs = reconnectInitialMs;
    }

    public long getReconnectMaxMs() {
        return reconnectMaxMs;
    }

    public void setReconnectMaxMs(long reconnectMaxMs) {
        this.reconnectMaxMs = reconnectMaxMs;
    }

    public String getWelcomeText() {
        return welcomeText;
    }

    public void setWelcomeText(String welcomeText) {
        this.welcomeText = welcomeText;
    }

    public boolean hasCredentials() {
        return botId != null && !botId.isBlank()
                && secret != null && !secret.isBlank();
    }
}
