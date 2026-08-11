package com.github.walkvoid.zone.ai.business.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.models.ApiResult;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.zone.ai.business.db.dao.McpServerConfigDAO;
import com.github.walkvoid.zone.ai.business.service.McpServerConfigService;
import com.github.walkvoid.zone.ai.model.dto.McpServerConfigDTO;
import com.github.walkvoid.zone.ai.model.entity.McpServerConfig;
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
 * MCP Server闁板秶鐤?Controller
 *
 * @author walkvoid
 */
@Tag(name = "MCP Server闁板秶鐤?)
@RestController
@RequestMapping("/ai/mcp-server-config")
public class McpServerConfigController {

    @Autowired
    private McpServerConfigDAO dao;

    @Autowired
    private McpServerConfigService service;

    // ==================== CRUD ====================

    @Operation(summary = "閸掑棝銆夐弻銉嚄MCP閺堝秴濮熼柊宥囩枂")
    @GetMapping("/page")
    public ApiResult<PageDTO<McpServerConfigDTO>> page(PageRequest<McpServerConfigDTO> pageRequest) {
        PageDTO<McpServerConfigDTO> pageResult = dao.page(pageRequest);
        return ApiResult.ok(pageResult);
    }

    @Operation(summary = "閹稿D閺屻儴顕?)
    @GetMapping("/{id}")
    public ApiResult<McpServerConfigDTO> getById(@PathVariable("id") Long id) {
        McpServerConfig m = dao.selectById(id);
        return ApiResult.ok(m != null ? toDTO(m) : null);
    }

    @Operation(summary = "閺屻儴顕楅崗銊╁劥")
    @GetMapping("/list")
    public ApiResult<List<McpServerConfigDTO>> listAll() {
        List<McpServerConfig> list = dao.selectList(new McpServerConfig());
        List<McpServerConfigDTO> dtoList = list.stream().map(this::toDTO).collect(Collectors.toList());
        return ApiResult.ok(dtoList);
    }

    @Operation(summary = "閸掓稑缂揗CP閺堝秴濮熼柊宥囩枂")
    @PostMapping
    public ApiResult<String> create(@RequestBody McpServerConfigDTO dto) {
        if (dto.getServerCode() == null || dto.getServerCode().isBlank()) {
            return ApiResult.error(400, "閺堝秴濮熺紓鏍垳娑撳秷鍏樻稉铏光敄");
        }
        if (dao.checkCodeExists(dto.getServerCode()) > 0) {
            return ApiResult.error(400, "閺堝秴濮熺紓鏍垳瀹告彃鐡ㄩ崷?);
        }
        McpServerConfig entity = toEntity(dto);
        entity.setStatus(entity.getStatus() != null ? entity.getStatus() : 1);
        entity.setRunningStatus(0);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        dao.insert(entity);
        return ApiResult.ok("OK");
    }

    @Operation(summary = "閺囧瓨鏌奙CP閺堝秴濮熼柊宥囩枂")
    @PutMapping
    public ApiResult<String> update(@RequestBody McpServerConfigDTO dto) {
        if (dto.getId() == null) {
            return ApiResult.error(400, "ID娑撳秷鍏樻稉铏光敄");
        }
        McpServerConfig entity = toEntity(dto);
        entity.setUpdateTime(LocalDateTime.now());
        dao.updateById(entity);
        return ApiResult.ok("OK");
    }

    @Operation(summary = "閸掔娀娅嶮CP閺堝秴濮熼柊宥囩枂")
    @DeleteMapping("/{id}")
    public ApiResult<String> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ApiResult.ok("OK");
    }

    // ==================== 閸氼垰浠犻幒褍鍩?====================

    @Operation(summary = "閸氼垰濮㎝CP閺堝秴濮?)
    @PostMapping("/start/{serverCode}")
    public ApiResult<Map<String, Object>> start(@PathVariable("serverCode") String serverCode) {
        Map<String, Object> result = service.start(serverCode);
        return ApiResult.ok(result);
    }

    @Operation(summary = "閸嬫粍顒汳CP閺堝秴濮?)
    @PostMapping("/stop/{serverCode}")
    public ApiResult<Map<String, Object>> stop(@PathVariable("serverCode") String serverCode) {
        Map<String, Object> result = service.stop(serverCode);
        return ApiResult.ok(result);
    }

    @Operation(summary = "闁插秴鎯嶮CP閺堝秴濮?)
    @PostMapping("/restart/{serverCode}")
    public ApiResult<Map<String, Object>> restart(@PathVariable("serverCode") String serverCode) {
        Map<String, Object> result = service.restart(serverCode);
        return ApiResult.ok(result);
    }

    @Operation(summary = "閸氼垰濮╅崗銊╁劥瀹告彃鎯庨悽銊ф畱MCP閺堝秴濮?)
    @PostMapping("/start-all")
    public ApiResult<Map<String, Object>> startAll() {
        Map<String, Object> result = service.startAll();
        return ApiResult.ok(result);
    }

    @Operation(summary = "閸嬫粍顒涢崗銊╁劥鏉╂劘顢戞稉顓犳畱MCP閺堝秴濮?)
    @PostMapping("/stop-all")
    public ApiResult<Map<String, Object>> stopAll() {
        Map<String, Object> result = service.stopAll();
        return ApiResult.ok(result);
    }

    @Operation(summary = "閺屻儴顕桵CP閺堝秴濮熸潻鎰攽閻樿埖鈧?)
    @GetMapping("/status/{serverCode}")
    public ApiResult<Map<String, Object>> runningStatus(@PathVariable("serverCode") String serverCode) {
        Map<String, Object> result = service.getRunningStatus(serverCode);
        return ApiResult.ok(result);
    }

    @Operation(summary = "閺屻儴顕楅幍鈧張澶庣箥鐞涘奔鑵戦惃鍑狢P閺堝秴濮熺紓鏍垳")
    @GetMapping("/running-codes")
    public ApiResult<List<String>> runningCodes() {
        List<String> codes = service.listRunningCodes();
        return ApiResult.ok(codes);
    }

    // ==================== 閸愬懘鍎撮弬瑙勭《 ====================

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

