package com.github.walkvoid.zone.ai.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.walkvoid.zone.ai.business.db.mapper.AiAgentStepMapper;
import com.github.walkvoid.zone.ai.model.dto.AiAgentStepDTO;
import com.github.walkvoid.zone.ai.model.entity.AiAgentStep;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class AiAgentStepDAO {

    private final AiAgentStepMapper mapper;

    public AiAgentStepDAO(AiAgentStepMapper mapper) {
        this.mapper = mapper;
    }

    public int insert(AiAgentStep entity) {
        return mapper.insert(entity);
    }

    public int countByTurnNo(String turnNo) {
        if (!StringUtils.hasText(turnNo)) {
            return 0;
        }
        Long n = mapper.selectCount(new QueryWrapper<AiAgentStep>().eq("turn_no", turnNo.trim()));
        return n == null ? 0 : n.intValue();
    }

    public List<AiAgentStep> selectByTurnNo(String turnNo) {
        if (!StringUtils.hasText(turnNo)) {
            return List.of();
        }
        List<AiAgentStep> rows = mapper.selectList(new QueryWrapper<AiAgentStep>()
                .eq("turn_no", turnNo.trim())
                .orderByAsc("seq")
                .orderByAsc("id"));
        return rows == null ? List.of() : rows;
    }

    public static AiAgentStepDTO toDto(AiAgentStep entity) {
        if (entity == null) {
            return null;
        }
        AiAgentStepDTO dto = new AiAgentStepDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
