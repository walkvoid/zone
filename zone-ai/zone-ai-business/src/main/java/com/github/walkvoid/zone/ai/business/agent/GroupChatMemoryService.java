package com.github.walkvoid.zone.ai.business.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * 按群隔离的内存会话：窗口记忆、空闲 TTL、同会话串行。
 */
public class GroupChatMemoryService {

    private static final Logger log = LoggerFactory.getLogger(GroupChatMemoryService.class);

    private final AgentMemoryProperties properties;
    private final Clock clock;
    private final ChatMemory window;
    private final ChatMemory chatMemory;
    private final MessageChatMemoryAdvisor advisor;
    private final ConcurrentHashMap<String, Instant> lastAccess = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public GroupChatMemoryService(AgentMemoryProperties properties) {
        this(properties, Clock.systemUTC());
    }

    GroupChatMemoryService(AgentMemoryProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        this.window = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new TextOnlyChatMemoryRepository(properties.normalizedMaxMessageChars()))
                .maxMessages(properties.normalizedMaxMessages())
                .build();
        this.chatMemory = new TtlChatMemory();
        this.advisor = MessageChatMemoryAdvisor.builder(this.chatMemory).build();
        log.info("group chat memory ready, maxMessages={}, idleTtlMinutes={}, maxMessageChars={}",
                properties.normalizedMaxMessages(),
                properties.idleTtl().toMinutes(),
                properties.normalizedMaxMessageChars());
    }

    public MessageChatMemoryAdvisor advisor() {
        return advisor;
    }

    public ChatMemory chatMemory() {
        return chatMemory;
    }

    public void clear(String conversationId) {
        chatMemory.clear(conversationId);
        lastAccess.remove(conversationId);
        log.info("cleared conversation {}", conversationId);
    }

    public List<Message> get(String conversationId) {
        return chatMemory.get(conversationId);
    }

    public <T> T runExclusive(String conversationId, Supplier<T> action) {
        ReentrantLock lock = locks.computeIfAbsent(conversationId, id -> new ReentrantLock());
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }

    private final class TtlChatMemory implements ChatMemory {

        @Override
        public void add(String conversationId, List<Message> messages) {
            evictIdle();
            touch(conversationId);
            window.add(conversationId, messages);
        }

        @Override
        public List<Message> get(String conversationId) {
            evictIdle();
            if (expired(conversationId)) {
                window.clear(conversationId);
                lastAccess.remove(conversationId);
                return List.of();
            }
            touch(conversationId);
            return window.get(conversationId);
        }

        @Override
        public void clear(String conversationId) {
            window.clear(conversationId);
            lastAccess.remove(conversationId);
        }
    }

    private void touch(String conversationId) {
        lastAccess.put(conversationId, clock.instant());
    }

    private boolean expired(String conversationId) {
        Instant last = lastAccess.get(conversationId);
        if (last == null) {
            return false;
        }
        return Duration.between(last, clock.instant()).compareTo(properties.idleTtl()) >= 0;
    }

    private void evictIdle() {
        Instant now = clock.instant();
        Duration ttl = properties.idleTtl();
        for (String conversationId : List.copyOf(lastAccess.keySet())) {
            Instant last = lastAccess.get(conversationId);
            if (last == null) {
                continue;
            }
            if (Duration.between(last, now).compareTo(ttl) >= 0) {
                window.clear(conversationId);
                lastAccess.remove(conversationId);
                log.info("expired idle conversation {}", conversationId);
            }
        }
    }
}
