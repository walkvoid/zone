package com.github.walkvoid.zone.ai.business.service;

import com.github.walkvoid.zone.ai.model.entity.PromptTemplateRunRecord;
import java.util.List;

public interface PromptTemplateRunRecordService {
    int insert(PromptTemplateRunRecord entity);
    PromptTemplateRunRecord getById(Long id);
    List<PromptTemplateRunRecord> listByTemplateId(Long templateId);
}
