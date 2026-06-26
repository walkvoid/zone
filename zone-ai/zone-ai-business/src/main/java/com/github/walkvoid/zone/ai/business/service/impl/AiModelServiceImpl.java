package com.github.walkvoid.zone.ai.business.service.impl;

import com.github.walkvoid.zone.ai.api.service.AiModelService;
import com.github.walkvoid.zone.ai.business.db.dao.AiModelDAO;
import com.github.walkvoid.zone.ai.model.entity.AiModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiModelServiceImpl implements AiModelService {

    @Autowired
    private AiModelDAO aiModelDAO;

    @Override
    public AiModel getById(Long id) {
        return aiModelDAO.selectById(id);
    }

    @Override
    public AiModel getEnabled() {
        List<AiModel> models = aiModelDAO.selectEnabled();
        return (models != null && !models.isEmpty()) ? models.get(0) : null;
    }

    @Override
    public List<AiModel> listAll() {
        return aiModelDAO.selectList(new AiModel());
    }

    @Override
    public int insert(AiModel entity) {
        return aiModelDAO.insert(entity);
    }

    @Override
    public int update(AiModel entity) {
        return aiModelDAO.updateById(entity);
    }

    @Override
    public int delete(Long id) {
        return aiModelDAO.deleteById(id);
    }
}
