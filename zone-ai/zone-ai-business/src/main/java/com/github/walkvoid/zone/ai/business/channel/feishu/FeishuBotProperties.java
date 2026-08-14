package com.github.walkvoid.zone.ai.business.channel.feishu;

/**
 * 飞书机器人配置占位。后续实现长连接/事件订阅时填充字段。
 */
public class FeishuBotProperties {

    /**
     * 是否启用飞书通道（还需 zone.channel.enabled=true）。
     */
    private boolean enabled = false;

    /**
     * 应用 App ID（预留）。
     */
    private String appId = "";

    /**
     * 应用 App Secret（预留）。
     */
    private String appSecret = "";

    /**
     * WebSocket / 事件订阅地址（预留，以飞书官方文档为准）。
     */
    private String wsUrl = "";

    private String welcomeText = "你好，我是 Zone AI 助手。";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getWsUrl() {
        return wsUrl;
    }

    public void setWsUrl(String wsUrl) {
        this.wsUrl = wsUrl;
    }

    public String getWelcomeText() {
        return welcomeText;
    }

    public void setWelcomeText(String welcomeText) {
        this.welcomeText = welcomeText;
    }

    public boolean hasCredentials() {
        return appId != null && !appId.isBlank()
                && appSecret != null && !appSecret.isBlank();
    }
}
