package com.github.walkvoid.zone.ai.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.zone.ai.db.mapper.AiCodeChangeMapper;
import com.github.walkvoid.zone.ai.model.dto.AiCodeChangeDTO;
import com.github.walkvoid.zone.ai.db.entity.AiCodeChange;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class AiCodeChangeDAO {

    private final AiCodeChangeMapper mapper;

    public AiCodeChangeDAO(AiCodeChangeMapper mapper) {
        this.mapper = mapper;
    }

    public int insert(AiCodeChange entity) {
        return mapper.insert(entity);
    }

    public int updateById(AiCodeChange entity) {
        return mapper.updateById(entity);
    }

    public AiCodeChange selectById(Long id) {
        return mapper.selectById(id);
    }

    public AiCodeChange selectByTurnNo(String turnNo) {
        if (!StringUtils.hasText(turnNo)) {
            return null;
        }
        return mapper.selectOne(new QueryWrapper<AiCodeChange>()
                .eq("turn_no", turnNo.trim())
                .last("LIMIT 1"));
    }

    public PageDTO<AiCodeChangeDTO> page(PageRequest<AiCodeChangeDTO> pageRequest) {
        AiCodeChangeDTO param = pageRequest == null ? null : pageRequest.getParam();
        QueryWrapper<AiCodeChange> qw = new QueryWrapper<AiCodeChange>().orderByDesc("create_time");
        if (param != null) {
            if (StringUtils.hasText(param.getTurnNo())) {
                qw.eq("turn_no", param.getTurnNo().trim());
            }
            if (StringUtils.hasText(param.getConversationId())) {
                qw.like("conversation_id", param.getConversationId().trim());
            }
            if (StringUtils.hasText(param.getTitle())) {
                String keyword = param.getTitle().trim();
                qw.and(w -> w.like("title", keyword)
                        .or()
                        .like("request_text", keyword)
                        .or()
                        .like("conversation_id", keyword)
                        .or()
                        .like("bot_code", keyword));
            }
            if (StringUtils.hasText(param.getBotId())) {
                qw.eq("bot_id", param.getBotId().trim());
            }
            if (StringUtils.hasText(param.getWriteMode())) {
                qw.eq("write_mode", param.getWriteMode().trim());
            }
            if (param.getStatus() != null) {
                qw.eq("status", param.getStatus());
            }
        }
        long current = pageRequest == null ? 1L : Math.max(1L, pageRequest.getCurrent());
        int size = pageRequest == null ? 10 : Math.max(1, pageRequest.getSize());
        Page<AiCodeChange> mpPage = mapper.selectPage(new Page<>(current, size), qw);
        List<AiCodeChangeDTO> records = mpPage.getRecords().stream()
                .map(AiCodeChangeDAO::toDto)
                .collect(Collectors.toList());
        PageDTO<AiCodeChangeDTO> result = new PageDTO<>(mpPage.getCurrent(), mpPage.getSize(), mpPage.getTotal());
        result.setRecords(records);
        return result;
    }

    public static AiCodeChangeDTO toDto(AiCodeChange entity) {
        if (entity == null) {
            return null;
        }
        AiCodeChangeDTO dto = new AiCodeChangeDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
