package com.github.walkvoid.zone.ai.business.agent;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Agent 对话/工具审计。前缀 {@code zone.ai.agent.audit}。
 * 热路径只截断 JSON 并入队，写库在后台单线程。
 */
@ConfigurationProperties(prefix = "zone.ai.agent.audit")
public class AgentAuditProperties {

    private boolean enabled = true;
    private boolean async = true;
    private int queueCapacity = 3000;
    private int maxJsonBytes = 8192;
    private int shutdownDrainMs = 2000;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public int getMaxJsonBytes() {
        return maxJsonBytes;
    }

    public void setMaxJsonBytes(int maxJsonBytes) {
        this.maxJsonBytes = maxJsonBytes;
    }

    public int getShutdownDrainMs() {
        return shutdownDrainMs;
    }

    public void setShutdownDrainMs(int shutdownDrainMs) {
        this.shutdownDrainMs = shutdownDrainMs;
    }

    public int normalizedQueueCapacity() {
        return Math.max(64, queueCapacity);
    }

    public int normalizedMaxJsonBytes() {
        return Math.max(256, maxJsonBytes);
    }

    public int normalizedShutdownDrainMs() {
        return Math.max(0, shutdownDrainMs);
    }
}
