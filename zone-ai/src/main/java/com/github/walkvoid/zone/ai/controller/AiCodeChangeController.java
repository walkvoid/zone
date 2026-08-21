package com.github.walkvoid.zone.ai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.models.ApiResult;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.zone.ai.db.dao.AiCodeChangeDAO;
import com.github.walkvoid.zone.ai.db.dao.AiCodeChangePatchDAO;
import com.github.walkvoid.zone.ai.tool.repo.CodeChangeHistoryService;
import com.github.walkvoid.zone.ai.model.dto.AiCodeChangeDTO;
import com.github.walkvoid.zone.ai.model.dto.AiCodeChangePatchDTO;
import com.github.walkvoid.zone.ai.db.entity.AiCodeChange;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "AI 代码改动历史")
@RestController
@RequestMapping("/ai/ai-code-change")
public class AiCodeChangeController {

    private final AiCodeChangeDAO changeDAO;
    private final AiCodeChangePatchDAO patchDAO;
    private final CodeChangeHistoryService historyService;

    public AiCodeChangeController(AiCodeChangeDAO changeDAO,
                                  AiCodeChangePatchDAO patchDAO,
                                  CodeChangeHistoryService historyService) {
        this.changeDAO = changeDAO;
        this.patchDAO = patchDAO;
        this.historyService = historyService;
    }

    @Operation(summary = "分页查询改动记录（一条=一个功能点）")
    @GetMapping("/page")
    public ApiResult<PageDTO<AiCodeChangeDTO>> page(
            @RequestParam(value = "current", defaultValue = "1") long current,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @ModelAttribute AiCodeChangeDTO parameter) {
        return ApiResult.ok(changeDAO.page(PageRequest.of(current, size, parameter)));
    }

    @Operation(summary = "改动详情，含全部 patch")
    @GetMapping("/{id}")
    public ApiResult<AiCodeChangeDTO> getById(@PathVariable("id") Long id) {
        AiCodeChange entity = changeDAO.selectById(id);
        if (entity == null) {
            return ApiResult.ok(null);
        }
        AiCodeChangeDTO dto = AiCodeChangeDAO.toDto(entity);
        List<AiCodeChangePatchDTO> patches = patchDAO.selectByChangeId(id).stream()
                .map(AiCodeChangePatchDAO::toDto)
                .collect(Collectors.toList());
        dto.setPatches(patches);
        return ApiResult.ok(dto);
    }

    @Operation(summary = "把 DIFF_FILE 的 patch 写入沙箱源文件")
    @PostMapping("/{id}/apply")
    public ApiResult<CodeChangeHistoryService.ApplyOutcome> apply(@PathVariable("id") Long id) {
        try {
            return ApiResult.ok(historyService.apply(id));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ApiResult.error(400, e.getMessage());
        }
    }
}
