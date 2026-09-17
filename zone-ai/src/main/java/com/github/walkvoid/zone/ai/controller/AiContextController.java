package com.github.walkvoid.zone.ai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.models.ApiResult;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.zone.ai.model.dto.AiContextDTO;
import com.github.walkvoid.zone.ai.service.AiContextService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.List;

/**
 * AI 上下文
 */
@Tag(name = "AI上下文")
@RestController
@RequestMapping("/ai/ai-context")
public class AiContextController {

    private final AiContextService service;

    public AiContextController(AiContextService service) {
        this.service = service;
    }

    @Operation(summary = "分页查询上下文")
    @GetMapping("/page")
    public ApiResult<PageDTO<AiContextDTO>> page(
            @RequestParam(value = "current", defaultValue = "1") long current,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @ModelAttribute AiContextDTO parameter) {
        PageRequest<AiContextDTO> pageRequest = PageRequest.of(current, size, parameter);
        return ApiResult.ok(service.page(pageRequest));
    }

    @Operation(summary = "查询启用的上下文")
    @GetMapping("/enabled")
    public ApiResult<List<AiContextDTO>> enabled() {
        return ApiResult.ok(service.listEnabled());
    }

    @Operation(summary = "按 ID 查询")
    @GetMapping("/{id:\\d+}")
    public ApiResult<AiContextDTO> getById(@PathVariable("id") Long id) {
        return ApiResult.ok(service.getById(id));
    }

    @Operation(summary = "按路径查询")
    @GetMapping("/path")
    public ApiResult<AiContextDTO> getByContextPath(
            @RequestParam("contextPath") String contextPath) {
        return ApiResult.ok(service.getByContextPath(contextPath));
    }

    @Operation(summary = "创建上下文")
    @PostMapping
    public ApiResult<String> create(@RequestBody AiContextDTO dto) {
        try {
            Long id = service.create(dto);
            return ApiResult.ok(String.valueOf(id));
        } catch (IllegalArgumentException e) {
            return ApiResult.error(400, e.getMessage());
        }
    }

    @Operation(summary = "更新上下文")
    @PutMapping
    public ApiResult<String> update(@RequestBody AiContextDTO dto) {
        try {
            service.update(dto);
            return ApiResult.ok("OK");
        } catch (IllegalArgumentException e) {
            int code = "上下文不存在".equals(e.getMessage()) ? 404 : 400;
            return ApiResult.error(code, e.getMessage());
        }
    }

    @Operation(summary = "删除上下文")
    @DeleteMapping("/{id}")
    public ApiResult<String> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ApiResult.ok("OK");
    }
}
