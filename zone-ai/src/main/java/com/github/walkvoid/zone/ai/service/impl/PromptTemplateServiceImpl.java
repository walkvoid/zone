package com.github.walkvoid.zone.ai.service.impl;

import com.github.walkvoid.zone.ai.db.dao.PromptTemplateDAO;
import com.github.walkvoid.zone.ai.service.PromptTemplateService;
import com.github.walkvoid.zone.ai.db.entity.PromptTemplate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Prompt模板 Service 实现
 *
 * @author walkvoid
 */
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
