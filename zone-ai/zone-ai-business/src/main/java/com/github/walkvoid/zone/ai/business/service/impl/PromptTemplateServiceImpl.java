package com.github.walkvoid.zone.ai.business.service.impl;

import com.github.walkvoid.zone.ai.api.service.PromptTemplateService;
import com.github.walkvoid.zone.ai.business.db.dao.PromptTemplateDAO;
import com.github.walkvoid.zone.ai.model.entity.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromptTemplateServiceImpl implements PromptTemplateService {

    @Autowired
    private PromptTemplateDAO promptTemplateDAO;

    @Override
    public PromptTemplate getById(Long id) {
        return promptTemplateDAO.selectById(id);
    }

    @Override
    public PromptTemplate getByCode(String templateCode) {
        return promptTemplateDAO.selectByCode(templateCode);
    }

    @Override
    public List<PromptTemplate> listAll() {
        return promptTemplateDAO.selectList(new PromptTemplate());
    }

    @Override
    public int insert(PromptTemplate entity) {
        return promptTemplateDAO.insert(entity);
    }

    @Override
    public int update(PromptTemplate entity) {
        return promptTemplateDAO.updateById(entity);
    }

    @Override
    public int delete(Long id) {
        return promptTemplateDAO.deleteById(id);
    }
}
