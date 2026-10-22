package com.github.walkvoid.zone.ai.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.wvframework.utils.BeanCopyUtils;
import com.github.walkvoid.zone.ai.db.dao.PromptTemplateDAO;
import com.github.walkvoid.zone.ai.db.entity.PromptTemplate;
import com.github.walkvoid.zone.ai.model.dto.PromptTemplateDTO;
import com.github.walkvoid.zone.ai.service.PromptTemplateService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Prompt模板 Service 实现
 *
 * @author walkvoid
 */
@Service
public class PromptTemplateServiceImpl implements PromptTemplateService {

    private final PromptTemplateDAO promptTemplateDAO;

    public PromptTemplateServiceImpl(PromptTemplateDAO promptTemplateDAO) {
        this.promptTemplateDAO = promptTemplateDAO;
    }

    @Override
    public PageDTO<PromptTemplateDTO> page(PageRequest<PromptTemplateDTO> pageRequest) {
        return promptTemplateDAO.page(pageRequest);
    }

    @Override
    public PromptTemplateDTO getById(Long id) {
        return toDto(promptTemplateDAO.selectById(id));
    }

    @Override
    public PromptTemplateDTO getByCode(String templateCode) {
        return toDto(promptTemplateDAO.selectByCode(templateCode));
    }

    @Override
    public List<PromptTemplateDTO> listAll() {
        return toDtoList(promptTemplateDAO.selectList(new PromptTemplate()));
    }

    @Override
    public List<PromptTemplateDTO> listEnabled() {
        return toDtoList(promptTemplateDAO.selectEnabled());
    }

    @Override
    public boolean insert(PromptTemplateDTO dto) {
        if (dto == null || dto.getTemplateCode() == null || dto.getTemplateCode().isBlank()) {
            throw new IllegalArgumentException("模板编码不能为空");
        }
        if (promptTemplateDAO.checkCodeExists(dto.getTemplateCode()) > 0) {
            throw new IllegalArgumentException("模板编码已存在");
        }
        PromptTemplate entity = toEntity(dto);
        entity.setStatus(entity.getStatus() != null ? entity.getStatus() : 1);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        return promptTemplateDAO.insert(entity) > 0;
    }

    @Override
    public boolean update(PromptTemplateDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new IllegalArgumentException("ID不能为空");
        }
        PromptTemplate entity = toEntity(dto);
        entity.setUpdateTime(LocalDateTime.now());
        return promptTemplateDAO.updateById(entity) > 0;
    }

    @Override
    public boolean delete(Long id) {
        return promptTemplateDAO.deleteById(id) > 0;
    }

    private static PromptTemplate toEntity(PromptTemplateDTO dto) {
        PromptTemplate entity = BeanCopyUtils.copyBean(dto, PromptTemplate.class);
        return entity == null ? new PromptTemplate() : entity;
    }

    private static PromptTemplateDTO toDto(PromptTemplate entity) {
        return BeanCopyUtils.copyBean(entity, PromptTemplateDTO.class);
    }

    private static List<PromptTemplateDTO> toDtoList(List<PromptTemplate> entities) {
        return BeanCopyUtils.copyList(entities, PromptTemplateDTO.class);
    }
}
