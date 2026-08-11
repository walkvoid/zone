package com.github.walkvoid.zone.ai.business.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.models.ApiResult;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.zone.ai.business.db.dao.PromptTemplateRunRecordDAO;
import com.github.walkvoid.zone.ai.model.dto.PromptTemplateRunRecordDTO;
import com.github.walkvoid.zone.ai.model.entity.PromptTemplateRunRecord;
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
    public ApiResult<PageDTO<PromptTemplateRunRecordDTO>> page(
            @RequestParam(value = "current", defaultValue = "0") long current,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @ModelAttribute PromptTemplateRunRecordDTO parameter) {
        PageRequest<PromptTemplateRunRecordDTO> pageRequest = PageRequest.of(current, size, parameter);
        PageDTO<PromptTemplateRunRecordDTO> pageResult = dao.page(pageRequest);
        return ApiResult.ok(pageResult);
    }

    @Operation(summary = "按ID查询")
    @GetMapping("/{id}")
    public ApiResult<PromptTemplateRunRecordDTO> getById(@PathVariable("id") Long id) {
        PromptTemplateRunRecord m = dao.selectById(id);
        return ApiResult.ok(m != null ? toDTO(m) : null);
    }

    @Operation(summary = "按模板ID查询运行记录列表")
    @GetMapping("/template/{templateId}")
    public ApiResult<List<PromptTemplateRunRecordDTO>> listByTemplateId(@PathVariable("templateId") Long templateId) {
        List<PromptTemplateRunRecord> list = dao.selectByTemplateId(templateId);
        List<PromptTemplateRunRecordDTO> dtoList = list.stream().map(this::toDTO).toList();
        return ApiResult.ok(dtoList);
    }

    private PromptTemplateRunRecordDTO toDTO(PromptTemplateRunRecord entity) {
        PromptTemplateRunRecordDTO dto = new PromptTemplateRunRecordDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
