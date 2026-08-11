package com.github.walkvoid.zone.finance.business.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.models.ApiResult;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.zone.finance.api.service.StockInfoCrudService;
import com.github.walkvoid.zone.finance.model.dto.StockInfoDTO;
import com.github.walkvoid.zone.finance.model.dto.StockInfoQueryDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "股票管理")
@RestController
@RequestMapping("/finance/stock")
public class StockInfoController {

    @Autowired
    private StockInfoCrudService stockInfoCrudService;

    @Operation(summary = "分页查询股票列表")
    @GetMapping("/list")
    public ApiResult<Map<String, Object>> listPage(StockInfoQueryDTO query) {
        return ApiResult.ok(stockInfoCrudService.listPage(query));
    }

    @Operation(summary = "分页查询股票列表")
    @GetMapping("/page")
    public ApiResult<PageDTO<StockInfoDTO>> page(
            @RequestParam(value = "current", defaultValue = "0") long current,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @ModelAttribute StockInfoQueryDTO parameter) {
        PageRequest<StockInfoQueryDTO> pageRequest = PageRequest.of(current, size, parameter);
        PageDTO<StockInfoDTO> pageResult = stockInfoCrudService.page(pageRequest);
        return ApiResult.ok(pageResult);
    }

    @Operation(summary = "获取股票详情")
    @GetMapping("/{id}")
    public ApiResult<StockInfoDTO> getById(@Parameter(description = "股票ID") @PathVariable("id") Long id) {
        return ApiResult.ok(stockInfoCrudService.getById(id));
    }

    @Operation(summary = "按代码查询股票")
    @GetMapping("/code/{stockCode}")
    public ApiResult<StockInfoDTO> getByCode(@Parameter(description = "股票代码") @PathVariable("stockCode") String stockCode) {
        return ApiResult.ok(stockInfoCrudService.getByCode(stockCode));
    }

    @Operation(summary = "新增股票")
    @PostMapping
    public ApiResult<String> create(@RequestBody StockInfoDTO dto) {
        stockInfoCrudService.create(dto);
        return ApiResult.ok("OK");
    }

    @Operation(summary = "更新股票")
    @PutMapping("/{id}")
    public ApiResult<String> update(@Parameter(description = "股票ID") @PathVariable("id") Long id,
                                     @RequestBody StockInfoDTO dto) {
        stockInfoCrudService.update(id, dto);
        return ApiResult.ok("OK");
    }

    @Operation(summary = "删除股票")
    @DeleteMapping("/{id}")
    public ApiResult<String> delete(@Parameter(description = "股票ID") @PathVariable("id") Long id) {
        stockInfoCrudService.delete(id);
        return ApiResult.ok("OK");
    }
}
