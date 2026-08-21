package com.github.walkvoid.zone.ai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.models.ApiResult;
import com.github.walkvoid.wvframework.models.BooleanEnum;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.zone.ai.agent.AgentToolCode;
import com.github.walkvoid.zone.ai.db.dao.AiBotConfigDAO;
import com.github.walkvoid.zone.ai.model.dto.AiBotConfigDTO;
import com.github.walkvoid.zone.ai.db.entity.AiBotConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * 智能机器人配置（演示页 / 管理用）。密钥查询只回掩码。
 */
@Tag(name = "智能机器人配置")
@RestController
@RequestMapping("/ai/ai-bot-config")
public class AiBotConfigController {

    private final AiBotConfigDAO dao;

    public AiBotConfigController(AiBotConfigDAO dao) {
        this.dao = dao;
    }

    @Operation(summary = "分页查询机器人")
    @GetMapping("/page")
    public ApiResult<PageDTO<AiBotConfigDTO>> page(
            @RequestParam(value = "current", defaultValue = "1") long current,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @ModelAttribute AiBotConfigDTO parameter) {
        PageRequest<AiBotConfigDTO> pageRequest = PageRequest.of(current, size, parameter);
        return ApiResult.ok(dao.page(pageRequest));
    }

    @Operation(summary = "按 ID 查询")
    @GetMapping("/{id}")
    public ApiResult<AiBotConfigDTO> getById(@PathVariable("id") Long id) {
        return ApiResult.ok(AiBotConfigDAO.toDtoMasked(dao.selectById(id)));
    }

    @Operation(summary = "创建机器人")
    @PostMapping
    public ApiResult<String> create(@RequestBody AiBotConfigDTO dto) {
        String error = validateRequired(dto, false);
        if (error != null) {
            return ApiResult.error(400, error);
        }
        if (dao.codeExists(dto.getBotCode(), null)) {
            return ApiResult.error(400, "内部编码已存在");
        }
        if (dao.botIdExists(dto.getBotId(), null)) {
            return ApiResult.error(400, "企微 botId 已存在");
        }
        AiBotConfig entity = toEntity(dto);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        dao.insert(entity);
        return ApiResult.ok("OK");
    }

    @Operation(summary = "更新机器人；secret 留空则不改密钥")
    @PutMapping
    public ApiResult<String> update(@RequestBody AiBotConfigDTO dto) {
        if (dto.getId() == null) {
            return ApiResult.error(400, "ID不能为空");
        }
        String error = validateRequired(dto, true);
        if (error != null) {
            return ApiResult.error(400, error);
        }
        AiBotConfig existing = dao.selectById(dto.getId());
        if (existing == null) {
            return ApiResult.error(404, "机器人不存在");
        }
        if (dao.codeExists(dto.getBotCode(), dto.getId())) {
            return ApiResult.error(400, "内部编码已存在");
        }
        if (dao.botIdExists(dto.getBotId(), dto.getId())) {
            return ApiResult.error(400, "企微 botId 已存在");
        }
        AiBotConfig entity = toEntity(dto);
        entity.setId(dto.getId());
        entity.setUpdateTime(LocalDateTime.now());
        dao.updateById(entity);
        return ApiResult.ok("OK");
    }

    @Operation(summary = "删除机器人")
    @DeleteMapping("/{id}")
    public ApiResult<String> delete(@PathVariable("id") Long id) {
        dao.deleteById(id);
        return ApiResult.ok("OK");
    }

    private static String validateRequired(AiBotConfigDTO dto, boolean update) {
        if (dto == null) {
            return "请求体不能为空";
        }
        if (!StringUtils.hasText(dto.getBotCode())) {
            return "内部编码不能为空";
        }
        if (!StringUtils.hasText(dto.getBotId())) {
            return "企微 botId 不能为空";
        }
        if (!StringUtils.hasText(dto.getBotName())) {
            return "名称不能为空";
        }
        if (!StringUtils.hasText(dto.getSystemPrompt())) {
            return "系统提示词不能为空";
        }
        if (!update && dto.getSecret() != null && dto.getSecret().startsWith("••••")) {
            return "请填写真实 Secret，不要提交掩码";
        }
        return null;
    }

    private static AiBotConfig toEntity(AiBotConfigDTO dto) {
        AiBotConfig entity = new AiBotConfig();
        entity.setBotCode(dto.getBotCode().trim());
        entity.setBotId(dto.getBotId().trim());
        entity.setBotName(dto.getBotName().trim());
        entity.setChannelType(StringUtils.hasText(dto.getChannelType())
                ? dto.getChannelType().trim().toUpperCase()
                : "WEIXIN");
        entity.setSystemPrompt(dto.getSystemPrompt());
        entity.setToolCodes(normalizeToolCodes(dto.getToolCodes()));
        entity.setWelcomeText(dto.getWelcomeText());
        entity.setDescription(dto.getDescription());
        entity.setIsEnabled(dto.getIsEnabled() != null && dto.getIsEnabled() == 1
                ? BooleanEnum.YES
                : BooleanEnum.NO);
        if (isPlainSecret(dto.getSecret())) {
            entity.setSecret(dto.getSecret().trim());
        }
        return entity;
    }

    private static boolean isPlainSecret(String secret) {
        return StringUtils.hasText(secret) && !secret.trim().startsWith("••••");
    }

    private static String normalizeToolCodes(String raw) {
        return AgentToolCode.parse(raw).stream()
                .map(AgentToolCode::code)
                .collect(Collectors.joining(","));
    }
}
