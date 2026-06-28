package com.github.walkvoid.zone.finance.business.controller;

import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.wvframework.models.PageResponse;
import com.github.walkvoid.wvframework.models.WebPageResponse;
import com.github.walkvoid.wvframework.models.WebResponse;
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
    public WebResponse<Map<String, Object>> listPage(StockInfoQueryDTO query) {
        return WebResponse.ok(stockInfoCrudService.listPage(query));
    }

    @Operation(summary = "分页查询股票列表")
    @GetMapping("/page")
    public WebPageResponse<StockInfoDTO> page(
            @RequestParam(value = "current", defaultValue = "0") long current,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @ModelAttribute StockInfoQueryDTO parameter) {
        PageRequest<StockInfoQueryDTO> pageRequest = PageRequest.of(current, size, parameter);
        PageResponse<StockInfoDTO> pageResponse = stockInfoCrudService.page(pageRequest);
        return WebPageResponse.ok(pageResponse);
    }

    @Operation(summary = "获取股票详情")
    @GetMapping("/{id}")
    public WebResponse<StockInfoDTO> getById(@Parameter(description = "股票ID") @PathVariable("id") Long id) {
        return WebResponse.ok(stockInfoCrudService.getById(id));
    }

    @Operation(summary = "按代码查询股票")
    @GetMapping("/code/{stockCode}")
    public WebResponse<StockInfoDTO> getByCode(@Parameter(description = "股票代码") @PathVariable("stockCode") String stockCode) {
        return WebResponse.ok(stockInfoCrudService.getByCode(stockCode));
    }

    @Operation(summary = "新增股票")
    @PostMapping
    public WebResponse<Void> create(@RequestBody StockInfoDTO dto) {
        stockInfoCrudService.create(dto);
        return WebResponse.ok(null);
    }

    @Operation(summary = "更新股票")
    @PutMapping("/{id}")
    public WebResponse<Void> update(@Parameter(description = "股票ID") @PathVariable("id") Long id,
                                     @RequestBody StockInfoDTO dto) {
        stockInfoCrudService.update(id, dto);
        return WebResponse.ok(null);
    }

    @Operation(summary = "删除股票")
    @DeleteMapping("/{id}")
    public WebResponse<Void> delete(@Parameter(description = "股票ID") @PathVariable("id") Long id) {
        stockInfoCrudService.delete(id);
        return WebResponse.ok(null);
    }
}
