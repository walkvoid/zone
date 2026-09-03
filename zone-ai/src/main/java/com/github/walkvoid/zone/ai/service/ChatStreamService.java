package com.github.walkvoid.zone.ai.service;

import com.github.walkvoid.zone.ai.db.dao.AiContextPackDAO;
import com.github.walkvoid.zone.ai.db.dao.AiContextPackDetailDAO;
import com.github.walkvoid.zone.ai.db.dao.AiModelDAO;
import com.github.walkvoid.zone.ai.db.dao.PromptTemplateDAO;
import com.github.walkvoid.zone.ai.db.entity.AiContextPack;
import com.github.walkvoid.zone.ai.db.entity.AiContextPackDetail;
import com.github.walkvoid.zone.ai.db.entity.AiModel;
import com.github.walkvoid.zone.ai.db.entity.PromptTemplate;
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

    private static final int MAX_PACK_CHARS = 60_000;

    @Autowired
    private AiModelDAO aiModelDAO;

    @Autowired
    private PromptTemplateDAO promptTemplateDAO;

    @Autowired
    private AiContextPackDAO contextPackDAO;

    @Autowired
    private AiContextPackDetailDAO contextPackDetailDAO;

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
                LLMClient.buildMessages(buildSystemPrompt(request), messages);

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

    private String buildSystemPrompt(ChatStreamRequest request) {
        StringBuilder system = new StringBuilder();
        if (StringUtils.hasText(request.getSystemPrompt())) {
            system.append(request.getSystemPrompt().trim());
        } else if (request.getPromptTemplateId() != null) {
            PromptTemplate template = promptTemplateDAO.selectById(request.getPromptTemplateId());
            if (template == null) {
                throw new IllegalArgumentException("Prompt 模板不存在");
            }
            if (StringUtils.hasText(template.getTemplateContent())) {
                system.append(template.getTemplateContent().trim());
            }
        }

        if (request.getContextPackId() != null) {
            String packText = buildContextPackText(request.getContextPackId());
            if (StringUtils.hasText(packText)) {
                if (system.length() > 0) {
                    system.append("\n\n");
                }
                system.append(packText);
            }
        }
        return system.length() == 0 ? null : system.toString();
    }

    private String buildContextPackText(Long packId) {
        AiContextPack pack = contextPackDAO.selectById(packId);
        if (pack == null) {
            throw new IllegalArgumentException("上下文包不存在");
        }
        StringBuilder text = new StringBuilder();
        text.append("【上下文包】").append(StringUtils.hasText(pack.getPackName())
                ? pack.getPackName()
                : pack.getPackCode());
        if (StringUtils.hasText(pack.getSystemHint())) {
            text.append("\n").append(pack.getSystemHint().trim());
        }
        List<AiContextPackDetail> details = contextPackDetailDAO.selectByPackId(packId);
        for (AiContextPackDetail detail : details) {
            if (detail == null || !StringUtils.hasText(detail.getContent())) {
                continue;
            }
            text.append("\n\n");
            String title = StringUtils.hasText(detail.getTitle()) ? detail.getTitle().trim() : "材料";
            String type = StringUtils.hasText(detail.getDetailType()) ? detail.getDetailType().trim() : "OTHER";
            text.append("### ").append(title).append(" (").append(type).append(")\n");
            text.append(detail.getContent().trim());
        }
        if (text.length() > MAX_PACK_CHARS) {
            return text.substring(0, MAX_PACK_CHARS) + "\n…(上下文包过长，已截断)";
        }
        return text.toString();
    }

    private AiModel resolveModel(String modelCode) {
        if (StringUtils.hasText(modelCode)) {
            AiModel specified = aiModelDAO.selectByCode(modelCode.trim());
            if (specified != null) {
                return specified;
            }
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
