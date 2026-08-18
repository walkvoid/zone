package com.github.walkvoid.zone.ai.business.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.models.ApiResult;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.zone.ai.business.db.dao.AiAgentStepDAO;
import com.github.walkvoid.zone.ai.business.db.dao.AiAgentTurnDAO;
import com.github.walkvoid.zone.ai.model.dto.AiAgentStepDTO;
import com.github.walkvoid.zone.ai.model.dto.AiAgentTurnDTO;
import com.github.walkvoid.zone.ai.model.entity.AiAgentTurn;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Agent 对话日志")
@RestController
@RequestMapping("/ai/ai-agent-turn")
public class AiAgentTurnController {

    private final AiAgentTurnDAO turnDAO;
    private final AiAgentStepDAO stepDAO;

    public AiAgentTurnController(AiAgentTurnDAO turnDAO, AiAgentStepDAO stepDAO) {
        this.turnDAO = turnDAO;
        this.stepDAO = stepDAO;
    }

    @Operation(summary = "分页查询一轮问答")
    @GetMapping("/page")
    public ApiResult<PageDTO<AiAgentTurnDTO>> page(
            @RequestParam(value = "current", defaultValue = "1") long current,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @ModelAttribute AiAgentTurnDTO parameter) {
        return ApiResult.ok(turnDAO.page(PageRequest.of(current, size, parameter)));
    }

    @Operation(summary = "一轮问答详情，含工具步骤时间线")
    @GetMapping("/{id}")
    public ApiResult<AiAgentTurnDTO> getById(@PathVariable("id") Long id) {
        AiAgentTurn entity = turnDAO.selectById(id);
        if (entity == null) {
            return ApiResult.ok(null);
        }
        return ApiResult.ok(toDetail(entity));
    }

    @Operation(summary = "按 turnNo 查详情")
    @GetMapping("/turn/{turnNo}")
    public ApiResult<AiAgentTurnDTO> getByTurnNo(@PathVariable("turnNo") String turnNo) {
        AiAgentTurn entity = turnDAO.selectByTurnNo(turnNo);
        if (entity == null) {
            return ApiResult.ok(null);
        }
        return ApiResult.ok(toDetail(entity));
    }

    private AiAgentTurnDTO toDetail(AiAgentTurn entity) {
        AiAgentTurnDTO dto = AiAgentTurnDAO.toDto(entity);
        List<AiAgentStepDTO> steps = stepDAO.selectByTurnNo(entity.getTurnNo()).stream()
                .map(AiAgentStepDAO::toDto)
                .collect(Collectors.toList());
        dto.setSteps(steps);
        return dto;
    }
}
