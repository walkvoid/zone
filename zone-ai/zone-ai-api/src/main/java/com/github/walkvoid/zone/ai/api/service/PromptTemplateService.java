package com.github.walkvoid.zone.ai.api.service;

import com.github.walkvoid.zone.ai.model.entity.PromptTemplate;
import java.util.List;

public interface PromptTemplateService {
    PromptTemplate getById(Long id);
    PromptTemplate getByCode(String templateCode);
    List<PromptTemplate> listAll();
    int insert(PromptTemplate entity);
    int update(PromptTemplate entity);
    int delete(Long id);
}
