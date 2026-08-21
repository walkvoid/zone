package com.github.walkvoid.zone.ai.service.impl;

import com.github.walkvoid.zone.ai.service.PromptTemplateApi;
import com.github.walkvoid.zone.ai.db.dao.AiModelDAO;
import com.github.walkvoid.zone.ai.db.dao.PromptTemplateDAO;
import com.github.walkvoid.zone.ai.llm.LLMClient;
import com.github.walkvoid.zone.ai.model.dto.PromptTemplateDTO;
import com.github.walkvoid.zone.ai.db.entity.AiModel;
import com.github.walkvoid.zone.ai.db.entity.PromptTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Prompt模板 API 实现
 *
 * @author walkvoid
 */
@Service
public class PromptTemplateApiImpl implements PromptTemplateApi {

    private static final Logger log = LoggerFactory.getLogger(PromptTemplateApiImpl.class);

    @Autowired
    private PromptTemplateDAO promptTemplateDAO;

    @Autowired
    private AiModelDAO aiModelDAO;

    @Autowired
    private LLMClient llmClient;

    @Override
    public PromptTemplateDTO getByCode(String templateCode) {
        PromptTemplate entity = promptTemplateDAO.selectByCode(templateCode);
        if (entity == null) return null;
        PromptTemplateDTO dto = new PromptTemplateDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    @Override
    public String executePrompt(String templateCode, Map<String, String> variables) {
        // 1. 加载模板
        PromptTemplate template = promptTemplateDAO.selectByCode(templateCode);
        if (template == null) {
            throw new RuntimeException("Prompt模板 " + templateCode + " 不存在");
        }

        // 2. 填充变量；关联文档无 {document} 占位时追加到末尾
        String templateContent = template.getTemplateContent() == null ? "" : template.getTemplateContent();
        String userPrompt = templateContent;
        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                if (entry.getKey() == null || entry.getValue() == null) {
                    continue;
                }
                userPrompt = userPrompt.replace("{" + entry.getKey() + "}", entry.getValue());
            }
            if (variables.containsKey("document") && !templateContent.contains("{document}")) {
                userPrompt = userPrompt + "\n\n---\n【关联文档】\n" + variables.get("document");
            }
        }

        // 3. 获取启用的模型
        List<AiModel> models = aiModelDAO.selectEnabled();
        if (models == null || models.isEmpty()) {
            throw new RuntimeException("没有启用的AI模型");
        }
        AiModel aiModel = models.get(0);

        // 4. 调用大模型
        String systemPrompt = "你是一个专业的金融数据助手。请严格按要求的JSON格式返回数据，不要添加任何解释文字。";
        log.info("执行Prompt: code={}, model={}", templateCode, aiModel.getModelCode());
        return llmClient.chat(aiModel.getBaseUrl(), aiModel.getApiKey(), aiModel.getModelCode(),
                systemPrompt, userPrompt);
    }
}
