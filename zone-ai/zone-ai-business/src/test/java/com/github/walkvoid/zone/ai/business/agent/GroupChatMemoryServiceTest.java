package com.github.walkvoid.zone.ai.business.agent;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupChatMemoryServiceTest {

    @Test
    void isolatesConversationsAndClears() {
        GroupChatMemoryService service = new GroupChatMemoryService(props(20, 45));
        service.chatMemory().add("weixin:g1", List.of(
                new UserMessage("这笔融资"),
                new AssistantMessage("单号 123")));
        service.chatMemory().add("weixin:g2", List.of(
                new UserMessage("改代码"),
                new AssistantMessage("先看 write-mode")));

        assertEquals("这笔融资", service.get("weixin:g1").get(0).getText());
        assertEquals("改代码", service.get("weixin:g2").get(0).getText());

        service.clear("weixin:g1");
        assertTrue(service.get("weixin:g1").isEmpty());
        assertEquals(2, service.get("weixin:g2").size());
    }

    @Test
    void expiresAfterIdleTtl() {
        AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2026-08-17T00:00:00Z"));
        Clock clock = new Clock() {
            @Override
            public ZoneOffset getZone() {
                return ZoneOffset.UTC;
            }

            @Override
            public Clock withZone(java.time.ZoneId zone) {
                return this;
            }

            @Override
            public Instant instant() {
                return now.get();
            }
        };
        GroupChatMemoryService service = new GroupChatMemoryService(props(20, 45), clock);
        service.chatMemory().add("weixin:g1", List.of(
                new UserMessage("traceId abc"),
                new AssistantMessage("已记下")));
        assertEquals(2, service.get("weixin:g1").size());

        now.set(now.get().plus(Duration.ofMinutes(45)));
        assertTrue(service.get("weixin:g1").isEmpty());
    }

    private static AgentMemoryProperties props(int maxMessages, int idleMinutes) {
        AgentMemoryProperties properties = new AgentMemoryProperties();
        properties.setMaxMessages(maxMessages);
        properties.setIdleTtlMinutes(idleMinutes);
        properties.setMaxMessageChars(1500);
        return properties;
    }
}
