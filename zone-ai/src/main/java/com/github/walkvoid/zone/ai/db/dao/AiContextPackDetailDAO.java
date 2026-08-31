package com.github.walkvoid.zone.ai.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.walkvoid.zone.ai.db.entity.AiContextPackDetail;
import com.github.walkvoid.zone.ai.db.mapper.AiContextPackDetailMapper;
import com.github.walkvoid.zone.ai.model.dto.AiContextPackDetailDTO;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class AiContextPackDetailDAO {

    private final AiContextPackDetailMapper mapper;

    public AiContextPackDetailDAO(AiContextPackDetailMapper mapper) {
        this.mapper = mapper;
    }

    public int insert(AiContextPackDetail entity) {
        return mapper.insert(entity);
    }

    public int deleteByPackId(Long packId) {
        if (packId == null) {
            return 0;
        }
        return mapper.delete(new QueryWrapper<AiContextPackDetail>().eq("pack_id", packId));
    }

    public List<AiContextPackDetail> selectByPackId(Long packId) {
        if (packId == null) {
            return Collections.emptyList();
        }
        List<AiContextPackDetail> list = mapper.selectList(new QueryWrapper<AiContextPackDetail>()
                .eq("pack_id", packId)
                .orderByAsc("sort_order")
                .orderByAsc("id"));
        return list == null ? Collections.emptyList() : list;
    }

    public long countByPackId(Long packId) {
        if (packId == null) {
            return 0;
        }
        return mapper.selectCount(new QueryWrapper<AiContextPackDetail>().eq("pack_id", packId));
    }

    public static AiContextPackDetailDTO toDto(AiContextPackDetail entity) {
        if (entity == null) {
            return null;
        }
        AiContextPackDetailDTO dto = new AiContextPackDetailDTO();
        dto.setId(entity.getId());
        dto.setPackId(entity.getPackId());
        dto.setDetailType(entity.getDetailType());
        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContent());
        dto.setRefId(entity.getRefId());
        dto.setConfigJson(entity.getConfigJson());
        dto.setSortOrder(entity.getSortOrder());
        dto.setCreateId(entity.getCreateId());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateId(entity.getUpdateId());
        dto.setUpdateTime(entity.getUpdateTime());
        return dto;
    }

    public static AiContextPackDetail toEntity(AiContextPackDetailDTO dto, Long packId) {
        AiContextPackDetail entity = new AiContextPackDetail();
        if (dto == null) {
            return entity;
        }
        entity.setPackId(packId);
        entity.setDetailType(dto.getDetailType() == null ? null : dto.getDetailType().trim().toUpperCase());
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setRefId(dto.getRefId());
        entity.setConfigJson(dto.getConfigJson());
        entity.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        return entity;
    }

    public static List<AiContextPackDetailDTO> toDtoList(List<AiContextPackDetail> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().map(AiContextPackDetailDAO::toDto).collect(Collectors.toList());
    }
}
