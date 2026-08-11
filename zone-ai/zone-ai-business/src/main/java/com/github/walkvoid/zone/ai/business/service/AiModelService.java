package com.github.walkvoid.zone.ai.business.service;

import com.github.walkvoid.zone.ai.model.entity.AiModel;
import java.util.List;

/**
 * AI模型 Service
 *
 * @author walkvoid
 */
public interface AiModelService {

    /**
     * 根据ID查询
     */
    AiModel getById(Long id);

    /**
     * 获取启用的AI模型
     */
    AiModel getEnabled();

    /**
     * 查询全部AI模型
     */
    List<AiModel> listAll();

    /**
     * 新增AI模型
     */
    int insert(AiModel entity);

    /**
     * 更新AI模型
     */
    int update(AiModel entity);

    /**
     * 删除AI模型
     */
    int delete(Long id);
}
