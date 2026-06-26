package com.github.walkvoid.zone.ai.api.service;

import com.github.walkvoid.zone.ai.model.entity.AiModel;
import java.util.List;

public interface AiModelService {
    AiModel getById(Long id);
    AiModel getEnabled();
    List<AiModel> listAll();
    int insert(AiModel entity);
    int update(AiModel entity);
    int delete(Long id);
}
