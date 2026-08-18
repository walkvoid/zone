package com.github.walkvoid.zone.ai.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.walkvoid.zone.ai.business.db.mapper.AiCodeChangePatchMapper;
import com.github.walkvoid.zone.ai.model.dto.AiCodeChangePatchDTO;
import com.github.walkvoid.zone.ai.model.entity.AiCodeChangePatch;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class AiCodeChangePatchDAO {

    private final AiCodeChangePatchMapper mapper;

    public AiCodeChangePatchDAO(AiCodeChangePatchMapper mapper) {
        this.mapper = mapper;
    }

    public int insert(AiCodeChangePatch entity) {
        return mapper.insert(entity);
    }

    public int updateById(AiCodeChangePatch entity) {
        return mapper.updateById(entity);
    }

    public List<AiCodeChangePatch> selectByChangeId(Long changeId) {
        if (changeId == null) {
            return List.of();
        }
        List<AiCodeChangePatch> rows = mapper.selectList(new QueryWrapper<AiCodeChangePatch>()
                .eq("change_id", changeId)
                .orderByAsc("id"));
        return rows == null ? List.of() : rows;
    }

    public static AiCodeChangePatchDTO toDto(AiCodeChangePatch entity) {
        if (entity == null) {
            return null;
        }
        AiCodeChangePatchDTO dto = new AiCodeChangePatchDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
