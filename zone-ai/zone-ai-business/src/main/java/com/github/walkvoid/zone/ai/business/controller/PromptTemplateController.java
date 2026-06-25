package com.github.walkvoid.zone.ai.business.controller;

import com.github.walkvoid.wvframework.models.WebResponse;
import com.github.walkvoid.zone.ai.business.db.dao.PromptTemplateDAO;
import com.github.walkvoid.zone.ai.model.dto.PromptTemplateDTO;
import com.github.walkvoid.zone.ai.model.entity.PromptTemplate;
import com.github.walkvoid.zone.ai.model.enums.PromptStatusEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Prompt模板 Controller
 *
 * @author walkvoid
 */
@Tag(name = "Prompt模板管理")
@RestController
@RequestMapping("/prompt-template")
public class PromptTemplateController {

    private static final Pattern VAR_PATTERN = Pattern.compile("\\{\\{(\\w+)\\}\\}");

    @Autowired
    private PromptTemplateDAO dao;

    @Operation(summary = "分页查询模板列表")
    @GetMapping("/page")
    public WebResponse<List<PromptTemplateDTO>> page(PromptTemplateDTO query) {
        PromptTemplate condition = toEntity(query);
        List<PromptTemplate> list = dao.selectList(condition);
        List<PromptTemplateDTO> result = list.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return WebResponse.ok(result);
    }

    @Operation(summary = "按ID查询模板")
    @GetMapping("/{id}")
    public WebResponse<PromptTemplateDTO> getById(@PathVariable Long id) {
        PromptTemplate pt = dao.selectById(id);
        if (pt == null) {
            return WebResponse.of(404, "模板不存在", null, "warn");
        }
        return WebResponse.ok(toDTO(pt));
    }

    @Operation(summary = "按编码查询模板")
    @GetMapping("/code/{templateCode}")
    public WebResponse<PromptTemplateDTO> getByCode(@PathVariable String templateCode) {
        PromptTemplate pt = dao.selectByCode(templateCode);
        if (pt == null) {
            return WebResponse.of(404, "模板不存在", null, "warn");
        }
        return WebResponse.ok(toDTO(pt));
    }

    @Operation(summary = "创建模板")
    @PostMapping
    public WebResponse<Void> create(@RequestBody PromptTemplateDTO dto) {
        if (dto.getTemplateCode() == null || dto.getTemplateCode().isBlank()) {
            return WebResponse.of(400, "模板编码不能为空", null, "warn");
        }
        if (dto.getTemplateName() == null || dto.getTemplateName().isBlank()) {
            return WebResponse.of(400, "模板名称不能为空", null, "warn");
        }
        if (dao.checkCodeExists(dto.getTemplateCode()) > 0) {
            return WebResponse.of(400, "模板编码已存在", null, "warn");
        }

        PromptTemplate entity = toEntity(dto);
        // 自动提取变量
        entity.setVariables(extractVariables(entity.getTemplateContent()));
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        dao.insert(entity);
        return WebResponse.ok(null);
    }

    @Operation(summary = "更新模板")
    @PutMapping
    public WebResponse<Void> update(@RequestBody PromptTemplateDTO dto) {
        if (dto.getId() == null) {
            return WebResponse.of(400, "ID不能为空", null, "warn");
        }
        PromptTemplate existing = dao.selectById(dto.getId());
        if (existing == null) {
            return WebResponse.of(404, "模板不存在", null, "warn");
        }

        PromptTemplate entity = toEntity(dto);
        entity.setVariables(extractVariables(entity.getTemplateContent()));
        entity.setUpdateTime(LocalDateTime.now());
        dao.updateById(entity);
        return WebResponse.ok(null);
    }

    @Operation(summary = "删除模板")
    @DeleteMapping("/{id}")
    public WebResponse<Void> delete(@PathVariable Long id) {
        PromptTemplate existing = dao.selectById(id);
        if (existing == null) {
            return WebResponse.of(404, "模板不存在", null, "warn");
        }
        dao.deleteById(id);
        return WebResponse.ok(null);
    }

    // ==================== 辅助方法 ====================

    /**
     * 从模板内容中提取 {{变量名}}
     */
    private String extractVariables(String content) {
        if (content == null) return "[]";
        return VAR_PATTERN.matcher(content).results()
                .map(r -> "\"" + r.group(1) + "\"")
                .distinct()
                .collect(Collectors.joining(",", "[", "]"));
    }

    private PromptTemplate toEntity(PromptTemplateDTO dto) {
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
