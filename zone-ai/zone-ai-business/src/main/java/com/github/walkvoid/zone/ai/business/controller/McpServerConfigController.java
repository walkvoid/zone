package com.github.walkvoid.zone.ai.business.controller;

import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.wvframework.models.PageResponse;
import com.github.walkvoid.zone.ai.business.db.dao.McpServerConfigDAO;
import com.github.walkvoid.zone.ai.business.service.McpServerConfigService;
import com.github.walkvoid.zone.ai.model.dto.McpServerConfigDTO;
import com.github.walkvoid.zone.ai.model.entity.McpServerConfig;
import com.github.walkvoid.wvframework.models.WebPageResponse;
import com.github.walkvoid.wvframework.models.WebResponse;
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
 * MCP Server配置 Controller
 *
 * @author walkvoid
 */
@Tag(name = "MCP Server配置")
@RestController
@RequestMapping("/ai/mcp-server-config")
public class McpServerConfigController {

    @Autowired
    private McpServerConfigDAO dao;

    @Autowired
    private McpServerConfigService service;

    // ==================== CRUD ====================

    @Operation(summary = "分页查询MCP服务配置")
    @GetMapping("/page")
    public WebPageResponse<McpServerConfigDTO> page(PageRequest<McpServerConfigDTO> pageRequest) {
        PageResponse<McpServerConfigDTO> pageResponse = dao.page(pageRequest);
        return WebPageResponse.ok(pageResponse);
    }

    @Operation(summary = "按ID查询")
    @GetMapping("/{id}")
    public WebResponse<McpServerConfigDTO> getById(@PathVariable("id") Long id) {
        McpServerConfig m = dao.selectById(id);
        return WebResponse.ok(m != null ? toDTO(m) : null);
    }

    @Operation(summary = "查询全部")
    @GetMapping("/list")
    public WebResponse<List<McpServerConfigDTO>> listAll() {
        List<McpServerConfig> list = dao.selectList(new McpServerConfig());
        List<McpServerConfigDTO> dtoList = list.stream().map(this::toDTO).collect(Collectors.toList());
        return WebResponse.ok(dtoList);
    }

    @Operation(summary = "创建MCP服务配置")
    @PostMapping
    public WebResponse<String> create(@RequestBody McpServerConfigDTO dto) {
        if (dto.getServerCode() == null || dto.getServerCode().isBlank()) {
            return WebResponse.of(400, "服务编码不能为空", null, "warn");
        }
        if (dao.checkCodeExists(dto.getServerCode()) > 0) {
            return WebResponse.of(400, "服务编码已存在", null, "warn");
        }
        McpServerConfig entity = toEntity(dto);
        entity.setStatus(entity.getStatus() != null ? entity.getStatus() : 1);
        entity.setRunningStatus(0);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        dao.insert(entity);
        return WebResponse.ok("OK");
    }

    @Operation(summary = "更新MCP服务配置")
    @PutMapping
    public WebResponse<String> update(@RequestBody McpServerConfigDTO dto) {
        if (dto.getId() == null) {
            return WebResponse.of(400, "ID不能为空", null, "warn");
        }
        McpServerConfig entity = toEntity(dto);
        entity.setUpdateTime(LocalDateTime.now());
        dao.updateById(entity);
        return WebResponse.ok("OK");
    }

    @Operation(summary = "删除MCP服务配置")
    @DeleteMapping("/{id}")
    public WebResponse<String> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return WebResponse.ok("OK");
    }

    // ==================== 启停控制 ====================

    @Operation(summary = "启动MCP服务")
    @PostMapping("/start/{serverCode}")
    public WebResponse<Map<String, Object>> start(@PathVariable("serverCode") String serverCode) {
        Map<String, Object> result = service.start(serverCode);
        return WebResponse.ok(result);
    }

    @Operation(summary = "停止MCP服务")
    @PostMapping("/stop/{serverCode}")
    public WebResponse<Map<String, Object>> stop(@PathVariable("serverCode") String serverCode) {
        Map<String, Object> result = service.stop(serverCode);
        return WebResponse.ok(result);
    }

    @Operation(summary = "重启MCP服务")
    @PostMapping("/restart/{serverCode}")
    public WebResponse<Map<String, Object>> restart(@PathVariable("serverCode") String serverCode) {
        Map<String, Object> result = service.restart(serverCode);
        return WebResponse.ok(result);
    }

    @Operation(summary = "启动全部已启用的MCP服务")
    @PostMapping("/start-all")
    public WebResponse<Map<String, Object>> startAll() {
        Map<String, Object> result = service.startAll();
        return WebResponse.ok(result);
    }

    @Operation(summary = "停止全部运行中的MCP服务")
    @PostMapping("/stop-all")
    public WebResponse<Map<String, Object>> stopAll() {
        Map<String, Object> result = service.stopAll();
        return WebResponse.ok(result);
    }

    @Operation(summary = "查询MCP服务运行状态")
    @GetMapping("/status/{serverCode}")
    public WebResponse<Map<String, Object>> runningStatus(@PathVariable("serverCode") String serverCode) {
        Map<String, Object> result = service.getRunningStatus(serverCode);
        return WebResponse.ok(result);
    }

    @Operation(summary = "查询所有运行中的MCP服务编码")
    @GetMapping("/running-codes")
    public WebResponse<List<String>> runningCodes() {
        List<String> codes = service.listRunningCodes();
        return WebResponse.ok(codes);
    }

    // ==================== 内部方法 ====================

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
