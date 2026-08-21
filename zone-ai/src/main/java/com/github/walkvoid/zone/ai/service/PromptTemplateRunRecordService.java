package com.github.walkvoid.zone.ai.service;

import com.github.walkvoid.zone.ai.db.entity.PromptTemplateRunRecord;
import java.util.List;

/**
 * Prompt模板运行记录 Service
 *
 * @author walkvoid
 */
public interface PromptTemplateRunRecordService {

    /**
     * 新增运行记录
     */
    int insert(PromptTemplateRunRecord entity);

    /**
     * 根据ID查询
     */
    PromptTemplateRunRecord getById(Long id);

    /**
     * 根据模板ID查询运行记录列表
     */
    List<PromptTemplateRunRecord> listByTemplateId(Long templateId);
}
