package com.github.walkvoid.zone.ai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.models.ApiResult;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.zone.ai.db.dao.AiContextPackDAO;
import com.github.walkvoid.zone.ai.db.dao.AiContextPackDetailDAO;
import com.github.walkvoid.zone.ai.db.entity.AiContextPack;
import com.github.walkvoid.zone.ai.db.entity.AiContextPackDetail;
import com.github.walkvoid.zone.ai.model.dto.AiContextPackDTO;
import com.github.walkvoid.zone.ai.model.dto.AiContextPackDetailDTO;
import com.github.walkvoid.zone.ai.model.enums.ContextDetailType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 上下文包（一对多明细：FILE / CODE / OTHER）
 */
@Tag(name = "AI上下文包")
@RestController
@RequestMapping("/ai/ai-context-pack")
public class AiContextPackController {

    private final AiContextPackDAO packDAO;
    private final AiContextPackDetailDAO detailDAO;

    public AiContextPackController(AiContextPackDAO packDAO, AiContextPackDetailDAO detailDAO) {
        this.packDAO = packDAO;
        this.detailDAO = detailDAO;
    }

    @Operation(summary = "分页查询上下文包")
    @GetMapping("/page")
    public ApiResult<PageDTO<AiContextPackDTO>> page(
            @RequestParam(value = "current", defaultValue = "1") long current,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @ModelAttribute AiContextPackDTO parameter) {
        PageRequest<AiContextPackDTO> pageRequest = PageRequest.of(current, size, parameter);
        PageDTO<AiContextPackDTO> pageResult = packDAO.page(pageRequest);
        if (pageResult.getRecords() != null) {
            for (AiContextPackDTO dto : pageResult.getRecords()) {
                dto.setDetailCount((int) detailDAO.countByPackId(dto.getId()));
            }
        }
        return ApiResult.ok(pageResult);
    }

    @Operation(summary = "查询启用的上下文包（含明细）")
    @GetMapping("/enabled")
    public ApiResult<List<AiContextPackDTO>> enabled() {
        List<AiContextPackDTO> list = packDAO.selectEnabled().stream()
                .map(this::toDtoWithDetails)
                .collect(Collectors.toList());
        return ApiResult.ok(list);
    }

    @Operation(summary = "按 ID 查询（含明细）")
    @GetMapping("/{id}")
    public ApiResult<AiContextPackDTO> getById(@PathVariable("id") Long id) {
        AiContextPack pack = packDAO.selectById(id);
        if (pack == null) {
            return ApiResult.ok(null);
        }
        return ApiResult.ok(toDtoWithDetails(pack));
    }

    @Operation(summary = "按编码查询（含明细）")
    @GetMapping("/code/{packCode}")
    public ApiResult<AiContextPackDTO> getByCode(@PathVariable("packCode") String packCode) {
        AiContextPack pack = packDAO.selectByCode(packCode);
        if (pack == null) {
            return ApiResult.ok(null);
        }
        return ApiResult.ok(toDtoWithDetails(pack));
    }

    @Operation(summary = "创建上下文包（可带明细）")
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> create(@RequestBody AiContextPackDTO dto) {
        String error = validatePack(dto, false);
        if (error != null) {
            return ApiResult.error(400, error);
        }
        error = validateDetails(dto.getDetails());
        if (error != null) {
            return ApiResult.error(400, error);
        }
        if (packDAO.codeExists(dto.getPackCode(), null)) {
            return ApiResult.error(400, "上下文包编码已存在");
        }

        AiContextPack entity = AiContextPackDAO.toEntity(dto);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        packDAO.insert(entity);
        replaceDetails(entity.getId(), dto.getDetails(), now);
        return ApiResult.ok(String.valueOf(entity.getId()));
    }

    @Operation(summary = "更新上下文包（明细全量替换）")
    @PutMapping
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> update(@RequestBody AiContextPackDTO dto) {
        if (dto.getId() == null) {
            return ApiResult.error(400, "ID不能为空");
        }
        String error = validatePack(dto, true);
        if (error != null) {
            return ApiResult.error(400, error);
        }
        error = validateDetails(dto.getDetails());
        if (error != null) {
            return ApiResult.error(400, error);
        }
        AiContextPack existing = packDAO.selectById(dto.getId());
        if (existing == null) {
            return ApiResult.error(404, "上下文包不存在");
        }
        if (packDAO.codeExists(dto.getPackCode(), dto.getId())) {
            return ApiResult.error(400, "上下文包编码已存在");
        }

        AiContextPack entity = AiContextPackDAO.toEntity(dto);
        entity.setId(dto.getId());
        LocalDateTime now = LocalDateTime.now();
        entity.setUpdateTime(now);
        packDAO.updateById(entity);
        replaceDetails(dto.getId(), dto.getDetails(), now);
        return ApiResult.ok("OK");
    }

    @Operation(summary = "删除上下文包（级联明细）")
    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> delete(@PathVariable("id") Long id) {
        detailDAO.deleteByPackId(id);
        packDAO.deleteById(id);
        return ApiResult.ok("OK");
    }

    private AiContextPackDTO toDtoWithDetails(AiContextPack pack) {
        AiContextPackDTO dto = AiContextPackDAO.toDto(pack);
        List<AiContextPackDetailDTO> details =
                AiContextPackDetailDAO.toDtoList(detailDAO.selectByPackId(pack.getId()));
        dto.setDetails(details);
        dto.setDetailCount(details.size());
        return dto;
    }

    private void replaceDetails(Long packId, List<AiContextPackDetailDTO> details, LocalDateTime now) {
        detailDAO.deleteByPackId(packId);
        if (details == null || details.isEmpty()) {
            return;
        }
        int index = 0;
        for (AiContextPackDetailDTO item : details) {
            if (item == null) {
                continue;
            }
            AiContextPackDetail entity = AiContextPackDetailDAO.toEntity(item, packId);
            if (entity.getSortOrder() == null) {
                entity.setSortOrder(index);
            }
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
            detailDAO.insert(entity);
            index++;
        }
    }

    private static String validatePack(AiContextPackDTO dto, boolean update) {
        if (dto == null) {
            return "请求体不能为空";
        }
        if (!StringUtils.hasText(dto.getPackCode())) {
            return "上下文包编码不能为空";
        }
        if (!StringUtils.hasText(dto.getPackName())) {
            return "上下文包名称不能为空";
        }
        if (update && dto.getId() == null) {
            return "ID不能为空";
        }
        return null;
    }

    private static String validateDetails(List<AiContextPackDetailDTO> details) {
        if (details == null) {
            return null;
        }
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < details.size(); i++) {
            AiContextPackDetailDTO item = details.get(i);
            if (item == null) {
                continue;
            }
            ContextDetailType type = ContextDetailType.from(item.getDetailType());
            if (type == null) {
                errors.add("第" + (i + 1) + "条明细类型无效，应为 FILE / CODE / OTHER");
            }
        }
        return errors.isEmpty() ? null : String.join("; ", errors);
    }
}
