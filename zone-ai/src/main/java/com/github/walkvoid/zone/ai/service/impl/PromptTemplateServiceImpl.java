package com.github.walkvoid.zone.ai.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.wvframework.utils.BeanCopyUtils;
import com.github.walkvoid.zone.ai.db.dao.PromptTemplateDAO;
import com.github.walkvoid.zone.ai.db.entity.AiModel;
import com.github.walkvoid.zone.ai.db.entity.PromptTemplate;
import com.github.walkvoid.zone.ai.llm.LLMClient;
import com.github.walkvoid.zone.ai.model.dto.PromptTemplateDTO;
import com.github.walkvoid.zone.ai.service.AiModelService;
import com.github.walkvoid.zone.ai.service.PromptTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Prompt模板 Service 实现
 *
 * @author walkvoid
 */
@Service
public class PromptTemplateServiceImpl implements PromptTemplateService {

    private static final Logger log = LoggerFactory.getLogger(PromptTemplateServiceImpl.class);

    private final PromptTemplateDAO promptTemplateDAO;
    private final AiModelService aiModelService;
    private final LLMClient llmClient;

    public PromptTemplateServiceImpl(PromptTemplateDAO promptTemplateDAO,
                                     AiModelService aiModelService,
                                     LLMClient llmClient) {
        this.promptTemplateDAO = promptTemplateDAO;
        this.aiModelService = aiModelService;
        this.llmClient = llmClient;
    }

    @Override
    public PageDTO<PromptTemplateDTO> page(PageRequest<PromptTemplateDTO> pageRequest) {
        return promptTemplateDAO.page(pageRequest);
    }

    @Override
    public PromptTemplateDTO getById(Long id) {
        return toDto(promptTemplateDAO.selectById(id));
    }

    @Override
    public PromptTemplateDTO getByCode(String templateCode) {
        return toDto(promptTemplateDAO.selectByCode(templateCode));
    }

    @Override
    public List<PromptTemplateDTO> listAll() {
        return toDtoList(promptTemplateDAO.selectList(new PromptTemplate()));
    }

    @Override
    public List<PromptTemplateDTO> listEnabled() {
        return toDtoList(promptTemplateDAO.selectEnabled());
    }

    @Override
    public boolean insert(PromptTemplateDTO dto) {
        if (dto == null || dto.getTemplateCode() == null || dto.getTemplateCode().isBlank()) {
            throw new IllegalArgumentException("模板编码不能为空");
        }
        if (promptTemplateDAO.checkCodeExists(dto.getTemplateCode()) > 0) {
            throw new IllegalArgumentException("模板编码已存在");
        }
        PromptTemplate entity = toEntity(dto);
        entity.setStatus(entity.getStatus() != null ? entity.getStatus() : 1);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        return promptTemplateDAO.insert(entity) > 0;
    }

    @Override
    public boolean update(PromptTemplateDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new IllegalArgumentException("ID不能为空");
        }
        PromptTemplate entity = toEntity(dto);
        entity.setUpdateTime(LocalDateTime.now());
        return promptTemplateDAO.updateById(entity) > 0;
    }

    @Override
    public boolean delete(Long id) {
        return promptTemplateDAO.deleteById(id) > 0;
    }

    @Override
    public String executePrompt(String templateCode, Map<String, String> variables) {
        PromptTemplate template = promptTemplateDAO.selectByCode(templateCode);
        if (template == null) {
            throw new IllegalArgumentException("Prompt模板 " + templateCode + " 不存在");
        }
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
        AiModel aiModel = aiModelService.getEnabled();
        if (aiModel == null) {
            throw new IllegalArgumentException("没有启用的AI模型");
        }
        String systemPrompt = "你是一个专业的金融数据助手。请严格按要求的JSON格式返回数据，不要添加任何解释文字。";
        log.info("执行Prompt: code={}, model={}", templateCode, aiModel.getModelCode());
        return llmClient.chat(aiModel.getBaseUrl(), aiModel.getApiKey(), aiModel.getModelCode(),
                systemPrompt, userPrompt);
    }

    private static PromptTemplate toEntity(PromptTemplateDTO dto) {
        PromptTemplate entity = BeanCopyUtils.copyBean(dto, PromptTemplate.class);
        return entity == null ? new PromptTemplate() : entity;
    }

    private static PromptTemplateDTO toDto(PromptTemplate entity) {
        return BeanCopyUtils.copyBean(entity, PromptTemplateDTO.class);
    }

    private static List<PromptTemplateDTO> toDtoList(List<PromptTemplate> entities) {
        return BeanCopyUtils.copyList(entities, PromptTemplateDTO.class);
    }
}
