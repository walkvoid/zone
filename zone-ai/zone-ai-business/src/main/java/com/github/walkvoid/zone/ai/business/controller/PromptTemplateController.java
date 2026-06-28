package com.github.walkvoid.zone.ai.business.controller;

import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.wvframework.models.PageResponse;
import com.github.walkvoid.zone.ai.business.db.dao.PromptTemplateDAO;
import com.github.walkvoid.zone.ai.model.dto.PromptTemplateDTO;
import com.github.walkvoid.zone.ai.model.entity.PromptTemplate;
import com.github.walkvoid.wvframework.models.WebPageResponse;
import com.github.walkvoid.wvframework.models.WebResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Prompt模板管理 Controller
 *
 * @author walkvoid
 */
@Tag(name = "Prompt模板管理")
@RestController
@RequestMapping("/prompt-template")
public class PromptTemplateController {

    @Autowired
    private PromptTemplateDAO dao;

    @Operation(summary = "分页查询模板列表")
    @GetMapping("/page")
    public WebPageResponse<PromptTemplateDTO> page(
            @RequestParam(value = "current", defaultValue = "0") long current,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @ModelAttribute PromptTemplateDTO parameter) {
        PageRequest<PromptTemplateDTO> pageRequest = PageRequest.of(current, size, parameter);
        PageResponse<PromptTemplateDTO> pageResponse = dao.page(pageRequest);
        return WebPageResponse.ok(pageResponse);
    }

    @Operation(summary = "按ID查询")
    @GetMapping("/{id}")
    public WebResponse<PromptTemplateDTO> getById(@PathVariable("id") Long id) {
        PromptTemplate m = dao.selectById(id);
        return WebResponse.ok(m != null ? toDTO(m) : null);
    }

    @Operation(summary = "按编码查询")
    @GetMapping("/code/{templateCode}")
    public WebResponse<PromptTemplateDTO> getByCode(@PathVariable("templateCode") String templateCode) {
        PromptTemplate m = dao.selectByCode(templateCode);
        return WebResponse.ok(m != null ? toDTO(m) : null);
    }

    @Operation(summary = "创建模板")
    @PostMapping
    public WebResponse<String> create(@RequestBody PromptTemplateDTO dto) {
        if (dto.getTemplateCode() == null || dto.getTemplateCode().isBlank()) {
            return WebResponse.of(400, "模板编码不能为空", null, "warn");
        }
        if (dao.checkCodeExists(dto.getTemplateCode()) > 0) {
            return WebResponse.of(400, "模板编码已存在", null, "warn");
        }
        PromptTemplate entity = toEntity(dto);
        entity.setStatus(entity.getStatus() != null ? entity.getStatus() : 1);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        dao.insert(entity);
        return WebResponse.ok("OK");
    }

    @Operation(summary = "更新模板")
    @PutMapping
    public WebResponse<String> update(@RequestBody PromptTemplateDTO dto) {
        if (dto.getId() == null) {
            return WebResponse.of(400, "ID不能为空", null, "warn");
        }
        PromptTemplate entity = toEntity(dto);
        entity.setUpdateTime(LocalDateTime.now());
        dao.updateById(entity);
        return WebResponse.ok("OK");
    }

    @Operation(summary = "删除模板")
    @DeleteMapping("/{id}")
    public WebResponse<String> delete(@PathVariable("id") Long id) {
        dao.deleteById(id);
        return WebResponse.ok("OK");
    }

    private PromptTemplate toEntity(PromptTemplateDTO dto) {
        if (dto == null) {
            return new PromptTemplate();
        }
        PromptTemplate entity = new PromptTemplate();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    private PromptTemplateDTO toDTO(PromptTemplate entity) {
        PromptTemplateDTO dto = new PromptTemplateDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
