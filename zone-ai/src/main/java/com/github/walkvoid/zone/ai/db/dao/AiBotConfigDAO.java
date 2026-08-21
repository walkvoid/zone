package com.github.walkvoid.zone.ai.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.models.BooleanEnum;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.zone.ai.web.SecretDisplay;
import com.github.walkvoid.zone.ai.model.dto.AiBotConfigDTO;
import com.github.walkvoid.zone.ai.db.entity.AiBotConfig;
import com.github.walkvoid.zone.ai.db.mapper.AiBotConfigMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class AiBotConfigDAO {

    private final AiBotConfigMapper mapper;

    public AiBotConfigDAO(AiBotConfigMapper mapper) {
        this.mapper = mapper;
    }

    public int insert(AiBotConfig entity) {
        return mapper.insert(entity);
    }

    public int updateById(AiBotConfig entity) {
        return mapper.updateById(entity);
    }

    public int deleteById(Long id) {
        return mapper.deleteById(id);
    }

    public AiBotConfig selectById(Long id) {
        return mapper.selectById(id);
    }

    public AiBotConfig selectByBotId(String botId) {
        if (!StringUtils.hasText(botId)) {
            return null;
        }
        return mapper.selectOne(new QueryWrapper<AiBotConfig>()
                .eq("bot_id", botId.trim())
                .last("LIMIT 1"));
    }

    public AiBotConfig selectByBotCode(String botCode) {
        if (!StringUtils.hasText(botCode)) {
            return null;
        }
        return mapper.selectOne(new QueryWrapper<AiBotConfig>()
                .eq("bot_code", botCode.trim())
                .last("LIMIT 1"));
    }

    public List<AiBotConfig> selectEnabledByChannel(String channelType) {
        QueryWrapper<AiBotConfig> qw = new QueryWrapper<AiBotConfig>()
                .eq("is_enabled", 1)
                .orderByAsc("id");
        if (StringUtils.hasText(channelType)) {
            qw.eq("channel_type", channelType.trim());
        }
        List<AiBotConfig> rows = mapper.selectList(qw);
        return rows == null ? List.of() : rows;
    }

    public PageDTO<AiBotConfigDTO> page(PageRequest<AiBotConfigDTO> pageRequest) {
        AiBotConfigDTO param = pageRequest == null ? null : pageRequest.getParam();
        QueryWrapper<AiBotConfig> qw = new QueryWrapper<AiBotConfig>().orderByDesc("update_time");
        if (param != null) {
            if (StringUtils.hasText(param.getBotName())) {
                String keyword = param.getBotName().trim();
                qw.and(w -> w.like("bot_name", keyword)
                        .or()
                        .like("bot_code", keyword)
                        .or()
                        .like("bot_id", keyword));
            }
            if (StringUtils.hasText(param.getChannelType())) {
                qw.eq("channel_type", param.getChannelType().trim());
            }
            if (param.getIsEnabled() != null) {
                qw.eq("is_enabled", param.getIsEnabled());
            }
        }
        long current = pageRequest == null ? 1L : Math.max(1L, pageRequest.getCurrent());
        int size = pageRequest == null ? 10 : Math.max(1, pageRequest.getSize());
        Page<AiBotConfig> mpPage = mapper.selectPage(new Page<>(current, size), qw);
        List<AiBotConfigDTO> records = mpPage.getRecords().stream()
                .map(AiBotConfigDAO::toDtoMasked)
                .collect(Collectors.toList());
        PageDTO<AiBotConfigDTO> result = new PageDTO<>(mpPage.getCurrent(), mpPage.getSize(), mpPage.getTotal());
        result.setRecords(records);
        return result;
    }

    public boolean codeExists(String botCode, Long excludeId) {
        if (!StringUtils.hasText(botCode)) {
            return false;
        }
        QueryWrapper<AiBotConfig> qw = new QueryWrapper<AiBotConfig>().eq("bot_code", botCode.trim());
        if (excludeId != null) {
            qw.ne("id", excludeId);
        }
        return mapper.selectCount(qw) > 0;
    }

    public boolean botIdExists(String botId, Long excludeId) {
        if (!StringUtils.hasText(botId)) {
            return false;
        }
        QueryWrapper<AiBotConfig> qw = new QueryWrapper<AiBotConfig>().eq("bot_id", botId.trim());
        if (excludeId != null) {
            qw.ne("id", excludeId);
        }
        return mapper.selectCount(qw) > 0;
    }

    public static AiBotConfigDTO toDtoMasked(AiBotConfig entity) {
        if (entity == null) {
            return null;
        }
        AiBotConfigDTO dto = new AiBotConfigDTO();
        dto.setId(entity.getId());
        dto.setBotCode(entity.getBotCode());
        dto.setBotId(entity.getBotId());
        dto.setBotName(entity.getBotName());
        dto.setHasSecret(StringUtils.hasText(entity.getSecret()));
        dto.setSecret(SecretDisplay.mask(entity.getSecret()));
        dto.setChannelType(entity.getChannelType());
        dto.setSystemPrompt(entity.getSystemPrompt());
        dto.setToolCodes(entity.getToolCodes());
        dto.setWelcomeText(entity.getWelcomeText());
        dto.setIsEnabled(entity.getIsEnabled() == BooleanEnum.YES ? 1 : 0);
        dto.setDescription(entity.getDescription());
        dto.setCreateId(entity.getCreateId());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateId(entity.getUpdateId());
        dto.setUpdateTime(entity.getUpdateTime());
        return dto;
    }
}
