package com.github.walkvoid.zone.ai.business.controller;

import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.wvframework.models.PageResponse;
import com.github.walkvoid.zone.ai.business.db.dao.PromptTemplateRunRecordDAO;
import com.github.walkvoid.zone.ai.model.dto.PromptTemplateRunRecordDTO;
import com.github.walkvoid.zone.ai.model.entity.PromptTemplateRunRecord;
import com.github.walkvoid.wvframework.models.WebPageResponse;
import com.github.walkvoid.wvframework.models.WebResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PromptTemplate运行记录 Controller
 *
 * @author walkvoid
 */
@Tag(name = "PromptTemplate运行记录")
@RestController
@RequestMapping("/ai/prompt-template-run-record")
public class PromptTemplateRunRecordController {

    @Autowired
    private PromptTemplateRunRecordDAO dao;

    @Operation(summary = "分页查询运行记录")
    @GetMapping("/page")
    public WebPageResponse<PromptTemplateRunRecordDTO> page(
            @RequestParam(value = "current", defaultValue = "0") long current,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @ModelAttribute PromptTemplateRunRecordDTO parameter) {
        PageRequest<PromptTemplateRunRecordDTO> pageRequest = PageRequest.of(current, size, parameter);
        PageResponse<PromptTemplateRunRecordDTO> pageResponse = dao.page(pageRequest);
        return WebPageResponse.ok(pageResponse);
    }

    @Operation(summary = "按ID查询")
    @GetMapping("/{id}")
    public WebResponse<PromptTemplateRunRecordDTO> getById(@PathVariable("id") Long id) {
        PromptTemplateRunRecord m = dao.selectById(id);
        return WebResponse.ok(m != null ? toDTO(m) : null);
    }

    @Operation(summary = "按模板ID查询运行记录列表")
    @GetMapping("/template/{templateId}")
    public WebResponse<List<PromptTemplateRunRecordDTO>> listByTemplateId(@PathVariable("templateId") Long templateId) {
        List<PromptTemplateRunRecord> list = dao.selectByTemplateId(templateId);
        List<PromptTemplateRunRecordDTO> dtoList = list.stream().map(this::toDTO).toList();
        return WebResponse.ok(dtoList);
    }

    private PromptTemplateRunRecordDTO toDTO(PromptTemplateRunRecord entity) {
        PromptTemplateRunRecordDTO dto = new PromptTemplateRunRecordDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
