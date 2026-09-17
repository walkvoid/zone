package com.github.walkvoid.zone.ai.chatclient;

import tools.jackson.core.type.TypeReference;
import com.github.walkvoid.wvframework.utils.JsonUtils;
import com.github.walkvoid.wvframework.utils.StringUtils;
import com.github.walkvoid.zone.ai.db.entity.AiModel;
import com.github.walkvoid.zone.ai.db.entity.PromptTemplate;
import com.github.walkvoid.zone.ai.model.enums.PromptTemplateTypeEnum;
import com.github.walkvoid.zone.ai.service.PromptTemplateService;
import com.github.walkvoid.zone.ai.tool.FileUploadTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatProperties;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

public final class ChatClientConfigurer {

    private static final Logger log = LoggerFactory.getLogger(ChatClientConfigurer.class);

    private  String baseUrl;
    private  String apiKey;
    private  String model;
    private  List<String> optionModels;
    private  Double temperature;
    private  Duration readTimeout;
    private  String defaultSystem;
    private  String defaultUser;
    private  Object[] defaultsTools;

    private final Function<String[], Object[]> toolFinder;

    public ChatClientConfigurer() {
        this(null);
    }

    public ChatClientConfigurer(Function<String[], Object[]> toolFinder) {
        this.toolFinder = toolFinder;
    }

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
            if (aiModel.getMetadata() != null && aiModel.getMetadata().get("optionModels") instanceof Object oms){
                this.optionModels = JsonUtils.getObjectMapper().convertValue(oms, new TypeReference<List<String>>(){});
            }
            if (aiModel.getTemperature() != null){
                this.temperature = aiModel.getTemperature();
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

    public ChatClientConfigurer useDefaultSystem(String defaultSystem) {
        if (StringUtils.isNotEmpty(defaultSystem)){
            this.defaultSystem = defaultSystem;
        }
        return this;
    }

    public ChatClientConfigurer usePromptTemplate(PromptTemplate promptTemplate) {
        if (promptTemplate != null && StringUtils.isNotBlank(promptTemplate.getTemplateContent())){
            if (PromptTemplateTypeEnum.DEFAULT_SYSTEM.equals(promptTemplate.getType())){
                appendSystem(PromptTemplateService.apply(promptTemplate));
            }
            if (PromptTemplateTypeEnum.DEFAULT_USER.equals(promptTemplate.getType())){
                appendUser(PromptTemplateService.apply(promptTemplate));
            }
        }
        return this;
    }

    private void appendUser(String templateContent) {
        if (StringUtils.isNotEmpty(this.defaultUser)){
            this.defaultUser = this.defaultUser + "\n\n" + templateContent;
        }
    }

    private void appendSystem(String templateContent) {
        if (StringUtils.isNotEmpty(this.defaultSystem)){
            this.defaultSystem = this.defaultSystem + "\n\n" + templateContent;
        }
    }

    public ChatClientConfigurer usePromptTemplates(List<PromptTemplate> promptTemplates) {
        if (promptTemplates != null){
            for (PromptTemplate promptTemplate : promptTemplates){
                this.usePromptTemplate(promptTemplate);
            }
        }
        return this;
    }

    public ChatClientConfigurer useDefaultTools(String... toolNames) {
        if (toolNames != null && toolNames.length > 0 && toolFinder != null){
            this.defaultsTools =  toolFinder.apply(toolNames);
        }
        return this;
    }

    private FileUploadTool fileUploadTool;

    public ChatClientConfigurer applyDefaults(OpenAiChatProperties openAiChatProperties){
        return this;
    }

    public ChatClient create() {
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .options(OpenAiChatOptions.builder()
                        .baseUrl(this.baseUrl)
                        .apiKey(this.apiKey)
                        .model(this.model)
                        .temperature(this.temperature)
                        .build())
                .build();
        ChatClient.Builder builder = ChatClient.builder(openAiChatModel)
                .defaultSystem(this.defaultSystem);
        if (this.defaultsTools != null && this.defaultsTools.length > 0) {
            builder.defaultTools(this.defaultsTools);
        }
        return builder.build();
    }

}
