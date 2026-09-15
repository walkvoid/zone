package com.github.walkvoid.zone.ai.chatclient;

import com.github.walkvoid.wvframework.utils.StringUtils;
import com.github.walkvoid.zone.ai.db.entity.AiModel;
import com.github.walkvoid.zone.ai.model.dto.AiContextPackDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.chat.client.autoconfigure.ChatClientBuilderProperties;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatProperties;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;

import java.time.Duration;
import java.util.List;


public final class ChatClientConfigurer {
    private static final Logger log = LoggerFactory.getLogger(ChatClientConfigurer.class);

    private  String baseUrl;
    private  String apiKey;
    private  String model;
    private  List<String> optionModels;
    private  Double temperature;
    private  Duration readTimeout;
    private  String defaultSystem;


    public ChatClientConfigurer useAiModel(AiModel aiModel){
        if (aiModel != null){
            if (StringUtils.isNotEmpty(aiModel.getBaseUrl())){
                this.baseUrl = aiModel.getBaseUrl();
            }
            if (StringUtils.isNotEmpty(aiModel.getApiKey())){
                this.apiKey = aiModel.getApiKey();
            }
            if (StringUtils.isNotEmpty(aiModel.getModelName())){
                this.model = aiModel.getModelName();
            }
        }

       return this;
    }


    public ChatClientConfigurer useModel(String model) {
        if (StringUtils.isNotEmpty(model)){
            this.model = model;
        }
        return this;
    }

    public ChatClientConfigurer applyDefaults(OpenAiChatProperties openAiChatProperties){
        return this;
    }

    public ChatClient create(){
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .options(OpenAiChatOptions.builder()
                        .baseUrl(this.baseUrl)
                        .apiKey(this.apiKey)
                        .model(this.model)
                        .temperature(this.temperature)
                        .build())
                .build();
        ChatClient.builder(openAiChatModel)
                .defaultSystem(this.defaultSystem)

                .build();
        return  ChatClient.builder(openAiChatModel).build();
    }

}
