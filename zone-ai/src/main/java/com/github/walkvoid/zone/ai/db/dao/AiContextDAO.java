package com.github.walkvoid.zone.ai.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.models.BooleanEnum;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.zone.ai.db.entity.AiContext;
import com.github.walkvoid.zone.ai.db.mapper.AiContextMapper;
import com.github.walkvoid.zone.ai.model.dto.AiContextDTO;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class AiContextDAO {

    private final AiContextMapper mapper;

    public AiContextDAO(AiContextMapper mapper) {
        this.mapper = mapper;
    }

    public int insert(AiContext entity) {
        return mapper.insert(entity);
    }

    public int updateById(AiContext entity) {
        return mapper.updateById(entity);
    }

    public int deleteById(Long id) {
        return mapper.deleteById(id);
    }

    public AiContext selectById(Long id) {
        return mapper.selectById(id);
    }

    public AiContext selectByContextPath(String contextPath) {
        if (!StringUtils.hasText(contextPath)) {
            return null;
        }
        return mapper.selectOne(new QueryWrapper<AiContext>()
                .eq("context_path", contextPath.trim())
                .last("LIMIT 1"));
    }

    public boolean nameExists(String name, Long excludeId) {
        if (!StringUtils.hasText(name)) {
            return false;
        }
        QueryWrapper<AiContext> qw = new QueryWrapper<AiContext>().eq("name", name.trim());
        if (excludeId != null) {
            qw.ne("id", excludeId);
        }
        return mapper.selectCount(qw) > 0;
    }

    public boolean contextPathExists(String contextPath, Long excludeId) {
        if (!StringUtils.hasText(contextPath)) {
            return false;
        }
        QueryWrapper<AiContext> qw = new QueryWrapper<AiContext>()
                .eq("context_path", contextPath.trim());
        if (excludeId != null) {
            qw.ne("id", excludeId);
        }
        return mapper.selectCount(qw) > 0;
    }

    public List<AiContext> selectEnabled() {
        return mapper.selectList(new QueryWrapper<AiContext>()
                .eq("enabled", 1)
                .orderByDesc("update_time"));
    }

    public PageDTO<AiContextDTO> page(PageRequest<AiContextDTO> pageRequest) {
        AiContextDTO param = pageRequest == null ? null : pageRequest.getParam();
        QueryWrapper<AiContext> qw = new QueryWrapper<AiContext>().orderByDesc("update_time");
        if (param != null) {
            if (StringUtils.hasText(param.getName())) {
                qw.like("name", param.getName().trim());
            }
            if (StringUtils.hasText(param.getContextPath())) {
                qw.like("context_path", param.getContextPath().trim());
            }
            if (param.getEnabled() != null) {
                qw.eq("enabled", param.getEnabled());
            }
        }
        long current = pageRequest == null ? 1L : Math.max(1L, pageRequest.getCurrent());
        int size = pageRequest == null ? 10 : Math.max(1, pageRequest.getSize());
        Page<AiContext> mpPage = mapper.selectPage(new Page<>(current, size), qw);
        List<AiContextDTO> records = mpPage.getRecords().stream()
                .map(AiContextDAO::toDto)
                .collect(Collectors.toList());
        PageDTO<AiContextDTO> result =
                new PageDTO<>(mpPage.getCurrent(), mpPage.getSize(), mpPage.getTotal());
        result.setRecords(records);
        return result;
    }

    public static AiContextDTO toDto(AiContext entity) {
        if (entity == null) {
            return null;
        }
        AiContextDTO dto = new AiContextDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setContextPath(entity.getContextPath());
        dto.setSystemHint(entity.getSystemHint());
        dto.setDescription(entity.getDescription());
        dto.setEnabled(entity.getEnabled() != null ? entity.getEnabled().getKey() : 0);
        dto.setCreateId(entity.getCreateId());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateId(entity.getUpdateId());
        dto.setUpdateTime(entity.getUpdateTime());
        return dto;
    }

    public static AiContext toEntity(AiContextDTO dto) {
        AiContext entity = new AiContext();
        if (dto == null) {
            return entity;
        }
        entity.setId(dto.getId());
        entity.setName(dto.getName() == null ? null : dto.getName().trim());
        entity.setContextPath(dto.getContextPath() == null ? null : dto.getContextPath().trim());
        entity.setSystemHint(dto.getSystemHint());
        entity.setDescription(dto.getDescription());
        if (dto.getEnabled() != null) {
            entity.setEnabled(dto.getEnabled() == 1 ? BooleanEnum.YES : BooleanEnum.NO);
        } else {
            entity.setEnabled(BooleanEnum.YES);
        }
        return entity;
    }
}
