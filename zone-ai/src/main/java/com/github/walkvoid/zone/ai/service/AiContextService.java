package com.github.walkvoid.zone.ai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.zone.ai.model.dto.AiContextDTO;

import java.util.List;

/**
 * AI 上下文 Service
 */
public interface AiContextService {

    PageDTO<AiContextDTO> page(PageRequest<AiContextDTO> pageRequest);

    AiContextDTO getById(Long id);

    AiContextDTO getByContextPath(String contextPath);

    List<AiContextDTO> listEnabled();

    Long create(AiContextDTO dto);

    int update(AiContextDTO dto);

    int delete(Long id);
}
