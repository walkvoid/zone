package com.github.walkvoid.zone.ai.chatclient;

import com.github.walkvoid.zone.ai.db.entity.AiModel;
import org.junit.jupiter.api.Test;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatProperties;
import org.springframework.ai.model.openai.autoconfigure.OpenAiCommonProperties;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatClientConfigurerTest {

    @Test
    void fromModelParsesConfigJson() {
        AiModel model = new AiModel();
        model.setModelCode("qwen/qwen3.7-plus");
        model.setBaseUrl("https://example.com");
        model.setApiKey("sk-test");
        model.setConfigJson("{\"temperature\":0.2,\"readTimeout\":\"45s\"}");

        ChatClientConfigurer config = ChatClientConfigurer.fromModel(model);

        assertEquals("qwen/qwen3.7-plus", config.modelCode());
        assertEquals("https://example.com", config.baseUrl());
        assertEquals("sk-test", config.apiKey());
        assertEquals(0.2, config.temperature());
        assertEquals(Duration.ofSeconds(45), config.readTimeout());
        assertNull(config.defaultSystem());
    }

    @Test
    void withDefaultSystemIsImmutableCopy() {
        AiModel model = new AiModel();
        model.setModelCode("m1");
        model.setBaseUrl("https://a");
        model.setApiKey("k");
        ChatClientConfigurer base = ChatClientConfigurer.fromModel(model);

        ChatClientConfigurer withSystem = base.withDefaultSystem("你是助手");

        assertNull(base.defaultSystem());
        assertEquals("你是助手", withSystem.defaultSystem());
        assertTrue(base.sameConnection(withSystem));
    }

    @Test
    void fromPropertiesMergesCommonConnection() {
        OpenAiCommonProperties common = new OpenAiCommonProperties();
        common.setBaseUrl("https://common.example");
        common.setApiKey("common-key");

        OpenAiChatProperties chat = new OpenAiChatProperties();
        chat.setModel("deepseek/deepseek-v4-pro");
        chat.setTemperature(0.3);

        ChatClientConfigurer config = ChatClientConfigurer.fromProperties(chat, common);

        assertEquals("deepseek/deepseek-v4-pro", config.modelCode());
        assertEquals("https://common.example", config.baseUrl());
        assertEquals("common-key", config.apiKey());
        assertEquals(0.3, config.temperature());
    }
}
