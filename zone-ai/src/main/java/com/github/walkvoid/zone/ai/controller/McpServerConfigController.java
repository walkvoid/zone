package com.github.walkvoid.zone.ai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.models.ApiResult;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.zone.ai.db.dao.McpServerConfigDAO;
import com.github.walkvoid.zone.ai.service.McpServerConfigService;
import com.github.walkvoid.zone.ai.model.dto.McpServerConfigDTO;
import com.github.walkvoid.zone.ai.db.entity.McpServerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MCP Server Config Controller
 *
 * @author walkvoid
 */
@Tag(name = "MCP Server Config")
@RestController
@RequestMapping("/ai/mcp-server-config")
public class McpServerConfigController {

    @Autowired
    private McpServerConfigDAO dao;

    @Autowired
    private McpServerConfigService service;

    // ==================== CRUD ====================

    @Operation(summary = "Page query MCP server configs")
    @GetMapping("/page")
    public ApiResult<PageDTO<McpServerConfigDTO>> page(PageRequest<McpServerConfigDTO> pageRequest) {
        PageDTO<McpServerConfigDTO> pageResult = dao.page(pageRequest);
        return ApiResult.ok(pageResult);
    }

    @Operation(summary = "Get MCP server config by id")
    @GetMapping("/{id}")
    public ApiResult<McpServerConfigDTO> getById(@PathVariable("id") Long id) {
        McpServerConfig m = dao.selectById(id);
        return ApiResult.ok(m != null ? toDTO(m) : null);
    }

    @Operation(summary = "List MCP server configs")
    @GetMapping("/list")
    public ApiResult<List<McpServerConfigDTO>> listAll() {
        List<McpServerConfig> list = dao.selectList(new McpServerConfig());
        List<McpServerConfigDTO> dtoList = list.stream().map(this::toDTO).collect(Collectors.toList());
        return ApiResult.ok(dtoList);
    }

    @Operation(summary = "Create MCP server config")
    @PostMapping
    public ApiResult<String> create(@RequestBody McpServerConfigDTO dto) {
        if (dto.getServerCode() == null || dto.getServerCode().isBlank()) {
            return ApiResult.error(400, "Server code is required");
        }
        if (dao.checkCodeExists(dto.getServerCode()) > 0) {
            return ApiResult.error(400, "Server code already exists");
        }
        McpServerConfig entity = toEntity(dto);
        entity.setStatus(entity.getStatus() != null ? entity.getStatus() : 1);
        entity.setRunningStatus(0);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        dao.insert(entity);
        return ApiResult.ok("OK");
    }

    @Operation(summary = "Update MCP server config")
    @PutMapping
    public ApiResult<String> update(@RequestBody McpServerConfigDTO dto) {
        if (dto.getId() == null) {
            return ApiResult.error(400, "ID is required");
        }
        McpServerConfig entity = toEntity(dto);
        entity.setUpdateTime(LocalDateTime.now());
        dao.updateById(entity);
        return ApiResult.ok("OK");
    }

    @Operation(summary = "Delete MCP server config")
    @DeleteMapping("/{id}")
    public ApiResult<String> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ApiResult.ok("OK");
    }

    // ==================== Lifecycle Operations ====================

    @Operation(summary = "Start MCP server")
    @PostMapping("/start/{serverCode}")
    public ApiResult<Map<String, Object>> start(@PathVariable("serverCode") String serverCode) {
        Map<String, Object> result = service.start(serverCode);
        return ApiResult.ok(result);
    }

    @Operation(summary = "Stop MCP server")
    @PostMapping("/stop/{serverCode}")
    public ApiResult<Map<String, Object>> stop(@PathVariable("serverCode") String serverCode) {
        Map<String, Object> result = service.stop(serverCode);
        return ApiResult.ok(result);
    }

    @Operation(summary = "Restart MCP server")
    @PostMapping("/restart/{serverCode}")
    public ApiResult<Map<String, Object>> restart(@PathVariable("serverCode") String serverCode) {
        Map<String, Object> result = service.restart(serverCode);
        return ApiResult.ok(result);
    }

    @Operation(summary = "Start all MCP servers")
    @PostMapping("/start-all")
    public ApiResult<Map<String, Object>> startAll() {
        Map<String, Object> result = service.startAll();
        return ApiResult.ok(result);
    }

    @Operation(summary = "Stop all MCP servers")
    @PostMapping("/stop-all")
    public ApiResult<Map<String, Object>> stopAll() {
        Map<String, Object> result = service.stopAll();
        return ApiResult.ok(result);
    }

    @Operation(summary = "Get MCP server running status")
    @GetMapping("/status/{serverCode}")
    public ApiResult<Map<String, Object>> runningStatus(@PathVariable("serverCode") String serverCode) {
        Map<String, Object> result = service.getRunningStatus(serverCode);
        return ApiResult.ok(result);
    }

    @Operation(summary = "List running MCP server codes")
    @GetMapping("/running-codes")
    public ApiResult<List<String>> runningCodes() {
        List<String> codes = service.listRunningCodes();
        return ApiResult.ok(codes);
    }

    // ==================== DTO Converters ====================

    private McpServerConfig toEntity(McpServerConfigDTO dto) {
        if (dto == null) {
            return new McpServerConfig();
        }
        McpServerConfig entity = new McpServerConfig();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    private McpServerConfigDTO toDTO(McpServerConfig entity) {
        McpServerConfigDTO dto = new McpServerConfigDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}

