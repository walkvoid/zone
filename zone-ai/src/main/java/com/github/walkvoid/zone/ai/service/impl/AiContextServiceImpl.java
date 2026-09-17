package com.github.walkvoid.zone.ai.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.zone.ai.db.dao.AiContextDAO;
import com.github.walkvoid.zone.ai.db.entity.AiContext;
import com.github.walkvoid.zone.ai.model.dto.AiContextDTO;
import com.github.walkvoid.zone.ai.service.AiContextService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiContextServiceImpl implements AiContextService {

    private final AiContextDAO dao;

    public AiContextServiceImpl(AiContextDAO dao) {
        this.dao = dao;
    }

    @Override
    public PageDTO<AiContextDTO> page(PageRequest<AiContextDTO> pageRequest) {
        return dao.page(pageRequest);
    }

    @Override
    public AiContextDTO getById(Long id) {
        return AiContextDAO.toDto(dao.selectById(id));
    }

    @Override
    public AiContextDTO getByContextPath(String contextPath) {
        return AiContextDAO.toDto(dao.selectByContextPath(contextPath));
    }

    @Override
    public List<AiContextDTO> listEnabled() {
        return dao.selectEnabled().stream()
                .map(AiContextDAO::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Long create(AiContextDTO dto) {
        String error = validate(dto, false);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
        if (dao.nameExists(dto.getName(), null)) {
            throw new IllegalArgumentException("上下文名称已存在");
        }
        if (dao.contextPathExists(dto.getContextPath(), null)) {
            throw new IllegalArgumentException("上下文路径已存在");
        }
        AiContext entity = AiContextDAO.toEntity(dto);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        dao.insert(entity);
        return entity.getId();
    }

    @Override
    public int update(AiContextDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new IllegalArgumentException("ID不能为空");
        }
        String error = validate(dto, true);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
        AiContext existing = dao.selectById(dto.getId());
        if (existing == null) {
            throw new IllegalArgumentException("上下文不存在");
        }
        if (dao.nameExists(dto.getName(), dto.getId())) {
            throw new IllegalArgumentException("上下文名称已存在");
        }
        if (dao.contextPathExists(dto.getContextPath(), dto.getId())) {
            throw new IllegalArgumentException("上下文路径已存在");
        }
        AiContext entity = AiContextDAO.toEntity(dto);
        entity.setId(dto.getId());
        entity.setUpdateTime(LocalDateTime.now());
        return dao.updateById(entity);
    }

    @Override
    public int delete(Long id) {
        return dao.deleteById(id);
    }

    private static String validate(AiContextDTO dto, boolean update) {
        if (dto == null) {
            return "请求体不能为空";
        }
        if (!StringUtils.hasText(dto.getName())) {
            return "上下文名称不能为空";
        }
        if (!StringUtils.hasText(dto.getContextPath())) {
            return "上下文路径不能为空";
        }
        if (update && dto.getId() == null) {
            return "ID不能为空";
        }
        return null;
    }
}
