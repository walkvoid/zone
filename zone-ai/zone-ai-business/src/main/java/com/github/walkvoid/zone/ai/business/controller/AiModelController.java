package com.github.walkvoid.zone.ai.business.controller;

import com.github.walkvoid.zone.ai.business.db.dao.AiModelDAO;
import com.github.walkvoid.zone.ai.model.dto.AiModelDTO;
import com.github.walkvoid.zone.ai.model.entity.AiModel;
import com.github.walkvoid.wvframework.models.BooleanEnum;
import com.github.walkvoid.wvframework.models.WebResponse;
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
@RequestMapping("/ai-model")
public class AiModelController {

    @Autowired
    private AiModelDAO dao;

    @Operation(summary = "分页查询模型列表")
    @GetMapping("/page")
    public WebResponse<List<AiModelDTO>> page(AiModelDTO query) {
        List<AiModel> list = dao.selectList(toEntity(query));
        List<AiModelDTO> items = list.stream()
                .map(this::toDTO).collect(Collectors.toList());
        return WebResponse.ok(items);
    }

    @Operation(summary = "查询启用的模型（供AI调用使用）")
    @GetMapping("/enabled")
    public List<AiModelDTO> enabled() {
        return dao.selectEnabled().stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    @Operation(summary = "按ID查询")
    @GetMapping("/{id}")
    public AiModelDTO getById(@PathVariable Long id) {
        AiModel m = dao.selectById(id);
        return m != null ? toDTO(m) : null;
    }

    @Operation(summary = "按编码查询")
    @GetMapping("/code/{modelCode}")
    public AiModelDTO getByCode(@PathVariable String modelCode) {
        AiModel m = dao.selectByCode(modelCode);
        return m != null ? toDTO(m) : null;
    }

    @Operation(summary = "创建模型")
    @PostMapping
    public String create(@RequestBody AiModelDTO dto) {
        if (dto.getModelCode() == null || dto.getModelCode().isBlank()) {
            return "模型编码不能为空";
        }
        if (dao.checkCodeExists(dto.getModelCode()) > 0) {
            return "模型编码已存在";
        }
        AiModel entity = toEntity(dto);
        entity.setCallCount(0L);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        dao.insert(entity);
        return "OK";
    }

    @Operation(summary = "更新模型")
    @PutMapping
    public String update(@RequestBody AiModelDTO dto) {
        if (dto.getId() == null) return "ID不能为空";
        AiModel entity = toEntity(dto);
        entity.setUpdateTime(LocalDateTime.now());
        dao.updateById(entity);
        return "OK";
    }

    @Operation(summary = "删除模型")
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        dao.deleteById(id);
        return "OK";
    }

    private AiModel toEntity(AiModelDTO dto) {
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
