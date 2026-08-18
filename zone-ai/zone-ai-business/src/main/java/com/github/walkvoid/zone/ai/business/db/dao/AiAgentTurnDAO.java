package com.github.walkvoid.zone.ai.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.zone.ai.business.db.mapper.AiAgentTurnMapper;
import com.github.walkvoid.zone.ai.model.dto.AiAgentTurnDTO;
import com.github.walkvoid.zone.ai.model.entity.AiAgentTurn;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class AiAgentTurnDAO {

    private final AiAgentTurnMapper mapper;

    public AiAgentTurnDAO(AiAgentTurnMapper mapper) {
        this.mapper = mapper;
    }

    public int insert(AiAgentTurn entity) {
        return mapper.insert(entity);
    }

    public AiAgentTurn selectById(Long id) {
        return mapper.selectById(id);
    }

    public AiAgentTurn selectByTurnNo(String turnNo) {
        if (!StringUtils.hasText(turnNo)) {
            return null;
        }
        return mapper.selectOne(new QueryWrapper<AiAgentTurn>()
                .eq("turn_no", turnNo.trim())
                .last("LIMIT 1"));
    }

    public void incrementToolCallCount(String turnNo) {
        if (!StringUtils.hasText(turnNo)) {
            return;
        }
        mapper.update(null, new UpdateWrapper<AiAgentTurn>()
                .eq("turn_no", turnNo.trim())
                .setSql("tool_call_count = IFNULL(tool_call_count, 0) + 1"));
    }

    public void finish(String turnNo, int status, String finalAnswer, String errorMessage,
                       long durationMs, int toolCallCount) {
        if (!StringUtils.hasText(turnNo)) {
            return;
        }
        AiAgentTurn update = new AiAgentTurn();
        update.setStatus(status);
        update.setFinalAnswer(finalAnswer);
        update.setErrorMessage(errorMessage);
        update.setDurationMs(durationMs);
        update.setToolCallCount(Math.max(0, toolCallCount));
        update.setFinishTime(LocalDateTime.now());
        mapper.update(update, new UpdateWrapper<AiAgentTurn>().eq("turn_no", turnNo.trim()));
    }

    public PageDTO<AiAgentTurnDTO> page(PageRequest<AiAgentTurnDTO> pageRequest) {
        AiAgentTurnDTO param = pageRequest == null ? null : pageRequest.getParam();
        QueryWrapper<AiAgentTurn> qw = new QueryWrapper<AiAgentTurn>().orderByDesc("create_time");
        if (param != null) {
            if (StringUtils.hasText(param.getUserText())) {
                String keyword = param.getUserText().trim();
                qw.and(w -> w.like("user_text", keyword)
                        .or()
                        .like("conversation_id", keyword)
                        .or()
                        .like("session_id", keyword)
                        .or()
                        .like("bot_code", keyword)
                        .or()
                        .like("turn_no", keyword));
            }
            if (StringUtils.hasText(param.getConversationId())) {
                qw.eq("conversation_id", param.getConversationId().trim());
            }
            if (StringUtils.hasText(param.getSessionId())) {
                qw.eq("session_id", param.getSessionId().trim());
            }
            if (param.getStatus() != null) {
                qw.eq("status", param.getStatus());
            }
        }
        long current = pageRequest == null ? 1L : Math.max(1L, pageRequest.getCurrent());
        int size = pageRequest == null ? 10 : Math.max(1, pageRequest.getSize());
        Page<AiAgentTurn> mpPage = mapper.selectPage(new Page<>(current, size), qw);
        List<AiAgentTurnDTO> records = mpPage.getRecords().stream()
                .map(AiAgentTurnDAO::toDto)
                .collect(Collectors.toList());
        PageDTO<AiAgentTurnDTO> result = new PageDTO<>(mpPage.getCurrent(), mpPage.getSize(), mpPage.getTotal());
        result.setRecords(records);
        return result;
    }

    public static AiAgentTurnDTO toDto(AiAgentTurn entity) {
        if (entity == null) {
            return null;
        }
        AiAgentTurnDTO dto = new AiAgentTurnDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
