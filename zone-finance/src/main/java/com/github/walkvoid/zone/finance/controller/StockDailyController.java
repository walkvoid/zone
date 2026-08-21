package com.github.walkvoid.zone.finance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.models.ApiResult;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.zone.finance.service.StockDailyService;
import com.github.walkvoid.zone.finance.db.dao.StockDailyDAO;
import com.github.walkvoid.zone.finance.job.StockDailyJobHandler;
import com.github.walkvoid.zone.finance.model.dto.StockDailyQueryDTO;
import com.github.walkvoid.zone.finance.db.entity.StockDaily;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "股票日K线")
@RestController
@RequestMapping("/finance/stock/daily")
public class StockDailyController {

    @Autowired
    private StockDailyService stockDailyService;

    @Autowired
    private StockDailyJobHandler stockDailyJobHandler;

    @Autowired
    private StockDailyDAO stockDailyDAO;

    /**
     * 手动触发一次定时任务（测试用）
     */
    @PostMapping("/job/trigger")
    public ApiResult<String> triggerJob() {
        stockDailyJobHandler.execute();
        return ApiResult.ok("triggered");
    }

    /**
     * 调用大模型获取日K线数据
     * GET /stock/daily/fetch?stockCode=000001&startDate=2024-01-01&endDate=2024-01-05
     */
    @Operation(summary = "拉取并保存日K线数据")
    @GetMapping("/fetch")
    public ApiResult<List<StockDaily>> fetch(@RequestParam("stockCode") String stockCode,
                                  @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                  @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<StockDaily> list = stockDailyService.fetchAndSaveDaily(stockCode, startDate, endDate);
        return ApiResult.ok(list);
    }

    @Operation(summary = "分页查询日K线数据")
    @GetMapping("/page")
    public ApiResult<PageDTO<StockDaily>> page(
            @RequestParam(value = "current", defaultValue = "0") long current,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @ModelAttribute StockDailyQueryDTO parameter) {
        PageRequest<StockDailyQueryDTO> pageRequest = PageRequest.of(current, size, parameter);
        PageDTO<StockDaily> pageResult = stockDailyDAO.page(pageRequest);
        return ApiResult.ok(pageResult);
    }
}
