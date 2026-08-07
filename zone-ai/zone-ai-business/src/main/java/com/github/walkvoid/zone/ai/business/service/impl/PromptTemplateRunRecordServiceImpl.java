package com.github.walkvoid.zone.ai.business.service.impl;

import com.github.walkvoid.zone.ai.business.db.dao.PromptTemplateRunRecordDAO;
import com.github.walkvoid.zone.ai.business.service.PromptTemplateRunRecordService;
import com.github.walkvoid.zone.ai.model.entity.PromptTemplateRunRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromptTemplateRunRecordServiceImpl implements PromptTemplateRunRecordService {

    @Autowired
    private PromptTemplateRunRecordDAO dao;

    @Override
    public int insert(PromptTemplateRunRecord entity) {
        return dao.insert(entity);
    }

    @Override
    public PromptTemplateRunRecord getById(Long id) {
        return dao.selectById(id);
    }

    @Override
    public List<PromptTemplateRunRecord> listByTemplateId(Long templateId) {
        return dao.selectByTemplateId(templateId);
    }
}
