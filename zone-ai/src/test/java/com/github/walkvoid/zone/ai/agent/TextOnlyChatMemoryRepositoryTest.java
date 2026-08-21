package com.github.walkvoid.zone.ai.agent;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.MimeTypeUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextOnlyChatMemoryRepositoryTest {

    @Test
    void stripsImageBytesAndKeepsShortText() {
        TextOnlyChatMemoryRepository repo = new TextOnlyChatMemoryRepository(20);
        UserMessage withImage = UserMessage.builder()
                .text("看这张图")
                .media(new Media(MimeTypeUtils.IMAGE_JPEG, new ByteArrayResource(new byte[]{1, 2, 3, 4})))
                .build();
        repo.saveAll("weixin:g1", List.of(
                withImage,
                new AssistantMessage("a".repeat(50)),
                new AssistantMessage("  ")));

        List<Message> stored = repo.findByConversationId("weixin:g1");
        assertEquals(2, stored.size());
        assertEquals("看这张图（附图片）", stored.get(0).getText());
        assertTrue(stored.get(0) instanceof UserMessage user && user.getMedia().isEmpty());
        assertEquals("a".repeat(20) + "…", stored.get(1).getText());
        assertTrue(stored.get(1) instanceof AssistantMessage assistant && !assistant.hasToolCalls());
    }

    @Test
    void imageOnlyBecomesShortNote() {
        TextOnlyChatMemoryRepository repo = new TextOnlyChatMemoryRepository(1500);
        UserMessage imageOnly = UserMessage.builder()
                .text("")
                .media(new Media(MimeTypeUtils.IMAGE_PNG, new ByteArrayResource(new byte[]{9})))
                .build();
        repo.saveAll("weixin:g1", List.of(imageOnly));
        assertEquals("（用户发了一张图片）", repo.findByConversationId("weixin:g1").get(0).getText());
    }
}
