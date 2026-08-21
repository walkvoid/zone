package com.github.walkvoid.zone.ai.agent;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 企微群会话记忆。前缀 {@code zone.ai.agent.memory}。
 * 第一期只放内存，重启丢失。
 */
@ConfigurationProperties(prefix = "zone.ai.agent.memory")
public class AgentMemoryProperties {

    /**
     * 每个会话最多保留的消息条数（用户 + 助手各算一条，20 约等于 10 轮）。
     */
    private int maxMessages = 20;

    /**
     * 空闲超过该分钟数则清空该会话。
     */
    private int idleTtlMinutes = 45;

    /**
     * 写入记忆的单条文本上限，避免把工具 JSON / 长回复塞进上下文。
     */
    private int maxMessageChars = 1500;

    public int getMaxMessages() {
        return maxMessages;
    }

    public void setMaxMessages(int maxMessages) {
        this.maxMessages = maxMessages;
    }

    public int getIdleTtlMinutes() {
        return idleTtlMinutes;
    }

    public void setIdleTtlMinutes(int idleTtlMinutes) {
        this.idleTtlMinutes = idleTtlMinutes;
    }

    public int getMaxMessageChars() {
        return maxMessageChars;
    }

    public void setMaxMessageChars(int maxMessageChars) {
        this.maxMessageChars = maxMessageChars;
    }

    public int normalizedMaxMessages() {
        return Math.max(2, maxMessages);
    }

    public Duration idleTtl() {
        return Duration.ofMinutes(Math.max(1, idleTtlMinutes));
    }

    public int normalizedMaxMessageChars() {
        return Math.max(64, maxMessageChars);
    }
}
