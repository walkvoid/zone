package com.github.walkvoid.zone.ai.service;

import com.github.walkvoid.zone.ai.db.entity.PromptTemplate;
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
     * 根据ID查询
     */
    PromptTemplate getById(Long id);

    /**
     * 根据编码查询
     */
    PromptTemplate getByCode(String templateCode);

    /**
     * 查询全部模板
     */
    List<PromptTemplate> listAll();

    /**
     * 新增模板
     */
    int insert(PromptTemplate entity);

    /**
     * 更新模板
     */
    int update(PromptTemplate entity);

    /**
     * 删除模板
     */
    int delete(Long id);

    static String apply(PromptTemplate entity){
        return customRenderer.apply(entity.getTemplateContent(), entity.getVariables());
    }


}
