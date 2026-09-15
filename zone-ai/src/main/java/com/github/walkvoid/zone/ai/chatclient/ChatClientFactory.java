package com.github.walkvoid.zone.ai.chatclient;

import com.github.walkvoid.zone.ai.model.dto.AiContextPackDTO;
import com.github.walkvoid.zone.ai.model.dto.AiModelDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.model.chat.client.autoconfigure.ChatClientBuilderProperties;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatProperties;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * ChatClient 唯一构建入口：解析 yml / {@code ai_model} 规格，缓存 ChatModel，产出 Builder/Client。
 * <p>
 * 不拼上下文包、不挂业务 Tools / Memory Advisor；由调用方按次挂载。
 */
@Component
public class ChatClientFactory {
    private static final Logger log = LoggerFactory.getLogger(ChatClientFactory.class);

    private final OpenAiChatProperties openAiChatProperties;

    private final ChatClient.Builder builder;

    private final ConcurrentHashMap<String, ChatModel> cache = new ConcurrentHashMap<>();

    public ChatClientFactory(OpenAiChatProperties openAiChatProperties,ChatClient.Builder builder) {
        this.openAiChatProperties = openAiChatProperties;
        this.builder = builder;
    }

    public SmartChatClient get(ChatClientConfigurer configurer){
        if (configurer == null){
            configurer = new ChatClientConfigurer();
        }
        configurer.applyDefaults(openAiChatProperties);
        return new SmartChatClient(this, configurer, configurer.create());
    }

    public SmartChatClient get(){
        return null;
    }

    public SmartChatClient getFormAiModel(Long aiModelId){
        return null;
    }

    public SmartChatClient get(AiModelDTO aiModelDTO){
        return null;
    }

    public SmartChatClient getFormAiContextPack(Long aiContextPackId){
        return null;
    }

    public SmartChatClient get(AiContextPackDTO aiContextPack){
        return null;
    }

    public SmartChatClient getAndAttach(AiContextPackDTO pack, PromptTemplate promptTemplate){
        return null;
    }




}
