package com.github.walkvoid.zone.ai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.zone.ai.model.dto.PromptTemplateDTO;
import org.springframework.ai.template.st.StTemplateRenderer;

import java.util.List;

/**
 * Prompt模板 Service
 *
 * @author walkvoid
 */
public interface PromptTemplateService {

    StTemplateRenderer customRenderer = StTemplateRenderer.builder()
            .startDelimiterToken('<').endDelimiterToken('>').build();

    /**
     * 分页查询
     */
    PageDTO<PromptTemplateDTO> page(PageRequest<PromptTemplateDTO> pageRequest);

    /**
     * 根据ID查询
     */
    PromptTemplateDTO getById(Long id);

    /**
     * 根据编码查询
     */
    PromptTemplateDTO getByCode(String templateCode);

    /**
     * 查询全部模板
     */
    List<PromptTemplateDTO> listAll();

    /**
     * 查询启用的模板
     */
    List<PromptTemplateDTO> listEnabled();

    /**
     * 新增模板
     */
    boolean insert(PromptTemplateDTO dto);

    /**
     * 更新模板
     */
    boolean update(PromptTemplateDTO dto);

    /**
     * 删除模板
     */
    boolean delete(Long id);
}
