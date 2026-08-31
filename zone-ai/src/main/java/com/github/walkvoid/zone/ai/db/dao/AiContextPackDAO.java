package com.github.walkvoid.zone.ai.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.models.BooleanEnum;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.zone.ai.db.entity.AiContextPack;
import com.github.walkvoid.zone.ai.db.mapper.AiContextPackMapper;
import com.github.walkvoid.zone.ai.model.dto.AiContextPackDTO;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class AiContextPackDAO {

    private final AiContextPackMapper mapper;

    public AiContextPackDAO(AiContextPackMapper mapper) {
        this.mapper = mapper;
    }

    public int insert(AiContextPack entity) {
        return mapper.insert(entity);
    }

    public int updateById(AiContextPack entity) {
        return mapper.updateById(entity);
    }

    public int deleteById(Long id) {
        return mapper.deleteById(id);
    }

    public AiContextPack selectById(Long id) {
        return mapper.selectById(id);
    }

    public AiContextPack selectByCode(String packCode) {
        if (!StringUtils.hasText(packCode)) {
            return null;
        }
        return mapper.selectOne(new QueryWrapper<AiContextPack>()
                .eq("pack_code", packCode.trim())
                .last("LIMIT 1"));
    }

    public boolean codeExists(String packCode, Long excludeId) {
        if (!StringUtils.hasText(packCode)) {
            return false;
        }
        QueryWrapper<AiContextPack> qw = new QueryWrapper<AiContextPack>()
                .eq("pack_code", packCode.trim());
        if (excludeId != null) {
            qw.ne("id", excludeId);
        }
        return mapper.selectCount(qw) > 0;
    }

    public List<AiContextPack> selectEnabled() {
        return mapper.selectList(new QueryWrapper<AiContextPack>()
                .eq("is_enabled", 1)
                .orderByDesc("update_time"));
    }

    public PageDTO<AiContextPackDTO> page(PageRequest<AiContextPackDTO> pageRequest) {
        AiContextPackDTO param = pageRequest == null ? null : pageRequest.getParam();
        QueryWrapper<AiContextPack> qw = new QueryWrapper<AiContextPack>().orderByDesc("update_time");
        if (param != null) {
            if (StringUtils.hasText(param.getPackName())) {
                String keyword = param.getPackName().trim();
                qw.and(w -> w.like("pack_name", keyword)
                        .or()
                        .like("pack_code", keyword));
            }
            if (param.getIsEnabled() != null) {
                qw.eq("is_enabled", param.getIsEnabled());
            }
        }
        long current = pageRequest == null ? 1L : Math.max(1L, pageRequest.getCurrent());
        int size = pageRequest == null ? 10 : Math.max(1, pageRequest.getSize());
        Page<AiContextPack> mpPage = mapper.selectPage(new Page<>(current, size), qw);
        List<AiContextPackDTO> records = mpPage.getRecords().stream()
                .map(AiContextPackDAO::toDto)
                .collect(Collectors.toList());
        PageDTO<AiContextPackDTO> result =
                new PageDTO<>(mpPage.getCurrent(), mpPage.getSize(), mpPage.getTotal());
        result.setRecords(records);
        return result;
    }

    public static AiContextPackDTO toDto(AiContextPack entity) {
        if (entity == null) {
            return null;
        }
        AiContextPackDTO dto = new AiContextPackDTO();
        dto.setId(entity.getId());
        dto.setPackCode(entity.getPackCode());
        dto.setPackName(entity.getPackName());
        dto.setSystemHint(entity.getSystemHint());
        dto.setDescription(entity.getDescription());
        dto.setIsEnabled(entity.getIsEnabled() != null ? entity.getIsEnabled().getKey() : 0);
        dto.setCreateId(entity.getCreateId());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateId(entity.getUpdateId());
        dto.setUpdateTime(entity.getUpdateTime());
        return dto;
    }

    public static AiContextPack toEntity(AiContextPackDTO dto) {
        AiContextPack entity = new AiContextPack();
        if (dto == null) {
            return entity;
        }
        entity.setId(dto.getId());
        entity.setPackCode(dto.getPackCode() == null ? null : dto.getPackCode().trim());
        entity.setPackName(dto.getPackName() == null ? null : dto.getPackName().trim());
        entity.setSystemHint(dto.getSystemHint());
        entity.setDescription(dto.getDescription());
        if (dto.getIsEnabled() != null) {
            entity.setIsEnabled(dto.getIsEnabled() == 1 ? BooleanEnum.YES : BooleanEnum.NO);
        } else {
            entity.setIsEnabled(BooleanEnum.YES);
        }
        return entity;
    }
}
