package com.github.walkvoid.zone.ai.business.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.models.ApiResult;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.zone.ai.business.db.dao.AiModelDAO;
import com.github.walkvoid.zone.ai.model.dto.AiModelDTO;
import com.github.walkvoid.zone.ai.model.entity.AiModel;
import com.github.walkvoid.wvframework.models.BooleanEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI模型管理 Controller
 *
 * @author walkvoid
 */
@Tag(name = "AI模型管理")
@RestController
@RequestMapping("/ai/ai-model")
public class AiModelController {

    @Autowired
    private AiModelDAO aiModelDAO;

    @Operation(summary = "分页查询模型列表")
    @GetMapping("/page")
    public ApiResult<PageDTO<AiModelDTO>> page(
            @RequestParam(value = "current", defaultValue = "0") long current,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @ModelAttribute AiModelDTO parameter) {
        PageRequest<AiModelDTO> pageRequest = PageRequest.of(current, size, parameter);
        PageDTO<AiModelDTO> pageResult = aiModelDAO.page(pageRequest);
        return ApiResult.ok(pageResult);
    }

    @Operation(summary = "查询启用的模型（供AI调用使用）")
    @GetMapping("/enabled")
    public ApiResult<List<AiModelDTO>> enabled() {
        List<AiModelDTO> list = aiModelDAO.selectEnabled().stream()
                .map(this::toDTO).collect(Collectors.toList());
        return ApiResult.ok(list);
    }

    @Operation(summary = "按ID查询")
    @GetMapping("/{id}")
    public ApiResult<AiModelDTO> getById(@PathVariable("id") Long id) {
        AiModel m = aiModelDAO.selectById(id);
        return ApiResult.ok(m != null ? toDTO(m) : null);
    }

    @Operation(summary = "按编码查询")
    @GetMapping("/code/{modelCode}")
    public ApiResult<AiModelDTO> getByCode(@PathVariable("modelCode") String modelCode) {
        AiModel m = aiModelDAO.selectByCode(modelCode);
        return ApiResult.ok(m != null ? toDTO(m) : null);
    }

    @Operation(summary = "创建模型")
    @PostMapping
    public ApiResult<String> create(@RequestBody AiModelDTO dto) {
        if (dto.getModelCode() == null || dto.getModelCode().isBlank()) {
            return ApiResult.error(400, "模型编码不能为空");
        }
        if (aiModelDAO.checkCodeExists(dto.getModelCode()) > 0) {
            return ApiResult.error(400, "模型编码已存在");
        }
        AiModel entity = toEntity(dto);
        entity.setCallCount(0L);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        aiModelDAO.insert(entity);
        return ApiResult.ok("OK");
    }

    @Operation(summary = "更新模型")
    @PutMapping
    public ApiResult<String> update(@RequestBody AiModelDTO dto) {
        if (dto.getId() == null) return ApiResult.error(400, "ID不能为空");
        AiModel entity = toEntity(dto);
        entity.setUpdateTime(LocalDateTime.now());
        aiModelDAO.updateById(entity);
        return ApiResult.ok("OK");
    }

    @Operation(summary = "删除模型")
    @DeleteMapping("/{id}")
    public ApiResult<String> delete(@PathVariable("id") Long id) {
        aiModelDAO.deleteById(id);
        return ApiResult.ok("OK");
    }

    private AiModel toEntity(AiModelDTO dto) {
        if (dto == null) {
            return new AiModel();
        }
        AiModel entity = new AiModel();
        BeanUtils.copyProperties(dto, entity);
        if (dto.getIsEnabled() != null) {
            entity.setIsEnabled(dto.getIsEnabled() == 1 ? BooleanEnum.YES : BooleanEnum.NO);
        }
        return entity;
    }

    private AiModelDTO toDTO(AiModel entity) {
        AiModelDTO dto = new AiModelDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setIsEnabled(entity.getIsEnabled() != null ? entity.getIsEnabled().getKey() : 0);
        return dto;
    }
}
