package com.github.walkvoid.zone.ai.service;

import com.github.walkvoid.zone.ai.db.dao.AiModelDAO;
import com.github.walkvoid.zone.ai.db.entity.AiModel;
import com.github.walkvoid.zone.ai.llm.LLMClient;
import com.github.walkvoid.zone.ai.model.dto.ChatMessageItem;
import com.github.walkvoid.zone.ai.model.dto.ChatStreamRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Web 聊天流式服务：从 ai_model 取密钥，转发到大模型。
 */
@Service
public class ChatStreamService {

    @Autowired
    private AiModelDAO aiModelDAO;

    @Autowired
    private LLMClient llmClient;

    public void stream(ChatStreamRequest request, Consumer<String> onDelta) {
        if (request == null || request.getMessages() == null || request.getMessages().isEmpty()) {
            throw new IllegalArgumentException("messages 不能为空");
        }

        AiModel model = resolveModel(request.getModelCode());
        if (model == null) {
            throw new IllegalArgumentException("未找到可用模型，请先在「模型配置」中启用模型");
        }
        if (!StringUtils.hasText(model.getBaseUrl()) || !StringUtils.hasText(model.getApiKey())) {
            throw new IllegalArgumentException("模型 " + model.getModelCode() + " 缺少 baseUrl 或 apiKey");
        }

        List<Map<String, String>> messages = toMaps(request.getMessages());
        List<Map<String, String>> finalMessages =
                LLMClient.buildMessages(request.getSystemPrompt(), messages);

        llmClient.streamChat(
                model.getBaseUrl(),
                model.getApiKey(),
                model.getModelCode(),
                finalMessages,
                request.getTemperature() != null ? request.getTemperature() : 0.7,
                onDelta);

        if (model.getId() != null) {
            aiModelDAO.incrementCallCount(model.getId());
        }
    }

    private AiModel resolveModel(String modelCode) {
        if (StringUtils.hasText(modelCode)) {
            return aiModelDAO.selectByCode(modelCode.trim());
        }
        List<AiModel> enabled = aiModelDAO.selectEnabled();
        if (enabled == null || enabled.isEmpty()) {
            return null;
        }
        return enabled.get(0);
    }

    private static List<Map<String, String>> toMaps(List<ChatMessageItem> items) {
        List<Map<String, String>> list = new ArrayList<>();
        for (ChatMessageItem item : items) {
            if (item == null || !StringUtils.hasText(item.getRole())) {
                continue;
            }
            list.add(Map.of(
                    "role", item.getRole().trim(),
                    "content", item.getContent() == null ? "" : item.getContent()));
        }
        return list;
    }
}
