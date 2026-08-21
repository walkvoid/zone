package com.github.walkvoid.zone.ai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.models.ApiResult;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.wvframework.utils.JsonUtils;
import com.github.walkvoid.zone.ai.db.dao.AiModelDAO;
import com.github.walkvoid.zone.ai.db.dao.PromptTemplateDAO;
import com.github.walkvoid.zone.ai.db.dao.PromptTemplateRunRecordDAO;
import com.github.walkvoid.zone.ai.service.PromptTemplateApi;
import com.github.walkvoid.zone.ai.model.dto.PromptTemplateDTO;
import com.github.walkvoid.zone.ai.db.entity.AiModel;
import com.github.walkvoid.zone.ai.db.entity.PromptTemplate;
import com.github.walkvoid.zone.ai.db.entity.PromptTemplateRunRecord;
import com.github.walkvoid.zone.ai.prompt.PromptRunMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Prompt模板管理 Controller
 *
 * @author walkvoid
 */
@Tag(name = "Prompt模板管理")
@RestController
@RequestMapping("/ai/prompt-template")
public class PromptTemplateController {

    @Autowired
    private PromptTemplateDAO dao;

    @Autowired
    private PromptTemplateApi promptTemplateApi;

    @Autowired
    private PromptTemplateRunRecordDAO runRecordDAO;

    @Autowired
    private AiModelDAO aiModelDAO;

    @Autowired
    private PromptRunMaterialService materialService;

    @Value("${zone.ai.prompt.max-document-chars:30000}")
    private int maxDocumentChars;

    @Operation(summary = "分页查询模板列表")
    @GetMapping("/page")
    public ApiResult<PageDTO<PromptTemplateDTO>> page(
            @RequestParam(value = "current", defaultValue = "1") long current,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @ModelAttribute PromptTemplateDTO parameter) {
        PageRequest<PromptTemplateDTO> pageRequest = PageRequest.of(current, size, parameter);
        return ApiResult.ok(dao.page(pageRequest));
    }

    @Operation(summary = "按ID查询")
    @GetMapping("/{id}")
    public ApiResult<PromptTemplateDTO> getById(@PathVariable("id") Long id) {
        PromptTemplate m = dao.selectById(id);
        return ApiResult.ok(m != null ? toDTO(m) : null);
    }

    @Operation(summary = "按编码查询")
    @GetMapping("/code/{templateCode}")
    public ApiResult<PromptTemplateDTO> getByCode(@PathVariable("templateCode") String templateCode) {
        PromptTemplate m = dao.selectByCode(templateCode);
        return ApiResult.ok(m != null ? toDTO(m) : null);
    }

    @Operation(summary = "创建模板")
    @PostMapping
    public ApiResult<String> create(@RequestBody PromptTemplateDTO dto) {
        if (dto.getTemplateCode() == null || dto.getTemplateCode().isBlank()) {
            return ApiResult.error(400, "模板编码不能为空");
        }
        if (dao.checkCodeExists(dto.getTemplateCode()) > 0) {
            return ApiResult.error(400, "模板编码已存在");
        }
        PromptTemplate entity = toEntity(dto);
        entity.setStatus(entity.getStatus() != null ? entity.getStatus() : 1);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        dao.insert(entity);
        return ApiResult.ok("OK");
    }

    @Operation(summary = "更新模板")
    @PutMapping
    public ApiResult<String> update(@RequestBody PromptTemplateDTO dto) {
        if (dto.getId() == null) {
            return ApiResult.error(400, "ID不能为空");
        }
        PromptTemplate entity = toEntity(dto);
        entity.setUpdateTime(LocalDateTime.now());
        dao.updateById(entity);
        return ApiResult.ok("OK");
    }

    @Operation(summary = "删除模板")
    @DeleteMapping("/{id}")
    public ApiResult<String> delete(@PathVariable("id") Long id) {
        dao.deleteById(id);
        return ApiResult.ok("OK");
    }

    @Operation(summary = "运行模板；可关联多附件 fileIds。长文/docx 入临时向量再 RAG")
    @PostMapping("/run")
    public ApiResult<Map<String, Object>> run(@RequestBody Map<String, Object> request) {
        String templateCode = (String) request.get("templateCode");
        List<Long> fileIds = parseFileIds(request.get("fileIds"));
        boolean forceVector = Boolean.TRUE.equals(request.get("forceVector"))
                || "true".equalsIgnoreCase(String.valueOf(request.get("forceVector")));
        boolean cleanupVectors = Boolean.TRUE.equals(request.get("cleanupVectors"))
                || "true".equalsIgnoreCase(String.valueOf(request.get("cleanupVectors")));

        if (templateCode == null || templateCode.isBlank()) {
            return ApiResult.error(400, "模板编码不能为空");
        }

        PromptTemplate template = dao.selectByCode(templateCode);
        if (template == null) {
            return ApiResult.error(400, "模板不存在: " + templateCode);
        }

        Map<String, String> rawVariables = toStringMap(request.get("variables"));
        Map<String, String> variables = new HashMap<>(rawVariables);

        PromptRunMaterialService.PreparedMaterial material = null;
        if (!fileIds.isEmpty()) {
            try {
                String query = firstNonBlank(
                        variables.get("task"),
                        variables.get("question"),
                        variables.get("query"),
                        template.getTemplateName(),
                        templateCode);
                material = materialService.prepareMany(fileIds, query, forceVector);
                String doc = material.documentText();
                if (doc.length() > maxDocumentChars) {
                    doc = doc.substring(0, maxDocumentChars)
                            + "\n\n...[注入文本已截断，上限 " + maxDocumentChars + " 字符]";
                }
                variables.put("document", doc);
            } catch (IllegalArgumentException e) {
                return ApiResult.error(400, e.getMessage());
            } catch (Exception e) {
                return ApiResult.error(500, "处理关联文档失败: " + e.getMessage());
            }
        }

        String renderedPrompt = renderPrompt(template.getTemplateContent(), variables);

        String modelName = null;
        List<AiModel> models = aiModelDAO.selectEnabled();
        if (models != null && !models.isEmpty()) {
            modelName = models.get(0).getModelCode();
        }

        PromptTemplateRunRecord record = new PromptTemplateRunRecord();
        record.setTemplateId(template.getId());
        try {
            record.setFileIds(JsonUtils.getObjectMapper().writeValueAsString(fileIds));
        } catch (Exception e) {
            record.setFileIds("[]");
        }
        record.setRenderedPrompt(renderedPrompt);
        record.setModelName(modelName);
        record.setRunStartTime(LocalDateTime.now());
        try {
            Map<String, Object> input = new LinkedHashMap<>();
            input.put("variables", rawVariables);
            input.put("fileIds", fileIds);
            input.put("forceVector", forceVector);
            input.put("cleanupVectors", cleanupVectors);
            if (material != null) {
                input.put("vectorMode", material.vectorMode());
                input.put("chunkCount", material.chunkCount());
            }
            record.setInputParams(JsonUtils.getObjectMapper().writeValueAsString(input));
        } catch (Exception e) {
            record.setInputParams(String.valueOf(request));
        }

        try {
            String result = promptTemplateApi.executePrompt(templateCode, variables);
            record.setRunResult(result);
            record.setStatus(1);
        } catch (Exception e) {
            record.setStatus(0);
            record.setErrorMessage(e.getMessage());
        }

        record.setRunEndTime(LocalDateTime.now());
        record.setDurationMs(Duration.between(record.getRunStartTime(), record.getRunEndTime()).toMillis());
        record.setCreateTime(LocalDateTime.now());
        runRecordDAO.insert(record);

        boolean cleaned = false;
        if (cleanupVectors && material != null && material.vectorMode() && !fileIds.isEmpty()) {
            materialService.deleteByFileIds(fileIds);
            cleaned = true;
        }

        if (record.getStatus() != 1) {
            return ApiResult.error(500, record.getErrorMessage());
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("result", record.getRunResult());
        body.put("runRecordId", record.getId());
        body.put("fileIds", fileIds);
        body.put("vectorMode", material != null && material.vectorMode());
        body.put("chunkCount", material == null ? 0 : material.chunkCount());
        body.put("vectorsCleaned", cleaned);
        return ApiResult.ok(body);
    }

    @Operation(summary = "删除 Prompt 运行附件向量（body: {\"fileIds\":[1,2]}，不删 MinIO）")
    @DeleteMapping("/run-material")
    public ApiResult<Map<String, Object>> deleteRunMaterial(@RequestBody Map<String, Object> body) {
        List<Long> fileIds = parseFileIds(body == null ? null : body.get("fileIds"));
        materialService.deleteByFileIds(fileIds);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fileIds", fileIds);
        result.put("deleted", true);
        return ApiResult.ok(result);
    }

    static String renderPrompt(String templateContent, Map<String, String> variables) {
        String rendered = templateContent == null ? "" : templateContent;
        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                if (entry.getKey() == null || entry.getValue() == null) {
                    continue;
                }
                rendered = rendered.replace("{" + entry.getKey() + "}", entry.getValue());
            }
            if (variables.containsKey("document")
                    && (templateContent == null || !templateContent.contains("{document}"))) {
                rendered = rendered + "\n\n---\n【关联文档】\n" + variables.get("document");
            }
        }
        return rendered;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    static List<Long> parseFileIds(Object raw) {
        if (raw == null) {
            return List.of();
        }
        List<Long> result = new ArrayList<>();
        if (raw instanceof List<?> list) {
            for (Object item : list) {
                Long id = parseFileId(item);
                if (id != null) {
                    result.add(id);
                }
            }
            return result.stream().distinct().toList();
        }
        if (raw instanceof String s) {
            String text = s.trim();
            if (text.isEmpty()) {
                return List.of();
            }
            if (text.startsWith("[")) {
                try {
                    List<Object> arr = JsonUtils.getObjectMapper().readValue(text, List.class);
                    return parseFileIds(arr);
                } catch (Exception ignored) {
                    return List.of();
                }
            }
            for (String part : text.split("[,;\\s]+")) {
                Long id = parseFileId(part);
                if (id != null) {
                    result.add(id);
                }
            }
            return result.stream().distinct().toList();
        }
        Long single = parseFileId(raw);
        return single == null ? List.of() : List.of(single);
    }

    private static Long parseFileId(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number n) {
            return n.longValue();
        }
        String text = String.valueOf(raw).trim();
        if (text.isEmpty() || "null".equalsIgnoreCase(text)) {
            return null;
        }
        return Long.parseLong(text);
    }

    private static Map<String, String> toStringMap(Object raw) {
        Map<String, String> result = new HashMap<>();
        if (!(raw instanceof Map<?, ?> map)) {
            return result;
        }
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            result.put(String.valueOf(entry.getKey()),
                    entry.getValue() == null ? null : String.valueOf(entry.getValue()));
        }
        return result;
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
