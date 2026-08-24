package com.github.walkvoid.zone.ai.controller;

import com.github.walkvoid.wvframework.models.ApiResult;
import com.github.walkvoid.zone.ai.agent.AgentToolCode;
import com.github.walkvoid.zone.ai.agent.AgentToolRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统 Agent 工具目录（供管理端下拉选择等）。
 */
@Tag(name = "Agent 工具")
@RestController
@RequestMapping("/ai/tools")
public class AgentToolController {

    private final AgentToolRegistry agentToolRegistry;

    public AgentToolController(AgentToolRegistry agentToolRegistry) {
        this.agentToolRegistry = agentToolRegistry;
    }

    @Operation(summary = "列出系统工具（含未实现的预留项）")
    @GetMapping
    public ApiResult<List<Map<String, Object>>> list() {
        List<Map<String, Object>> items = new ArrayList<>();
        for (AgentToolCode code : AgentToolCode.values()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("code", code.code());
            row.put("label", code.label());
            row.put("description", code.description());
            boolean available = code.ready() && agentToolRegistry.isRegistered(code);
            row.put("available", available);
            row.put("ready", code.ready());
            items.add(row);
        }
        return ApiResult.ok(items);
    }
}
