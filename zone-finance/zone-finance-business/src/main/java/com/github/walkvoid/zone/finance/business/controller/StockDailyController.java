package com.github.walkvoid.zone.finance.business.controller;

import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.wvframework.models.PageResponse;
import com.github.walkvoid.wvframework.models.WebPageResponse;
import com.github.walkvoid.zone.finance.api.service.StockDailyService;
import com.github.walkvoid.zone.finance.business.db.dao.StockDailyDAO;
import com.github.walkvoid.zone.finance.business.job.StockDailyJobHandler;
import com.github.walkvoid.zone.finance.model.dto.StockDailyQueryDTO;
import com.github.walkvoid.zone.finance.model.entity.StockDaily;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "股票日K线")
@RestController
@RequestMapping("/stock/daily")
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
    public String triggerJob() {
        stockDailyJobHandler.execute();
        return "triggered";
    }

    /**
     * 调用大模型获取日K线数据
     * GET /stock/daily/fetch?stockCode=000001&startDate=2024-01-01&endDate=2024-01-05
     */
    @Operation(summary = "拉取并保存日K线数据")
    @GetMapping("/fetch")
    public List<StockDaily> fetch(@RequestParam("stockCode") String stockCode,
                                  @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                  @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return stockDailyService.fetchAndSaveDaily(stockCode, startDate, endDate);
    }

    @Operation(summary = "分页查询日K线数据")
    @GetMapping("/page")
    public WebPageResponse<StockDaily> page(
            @RequestParam(value = "current", defaultValue = "0") long current,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @ModelAttribute StockDailyQueryDTO parameter) {
        PageRequest<StockDailyQueryDTO> pageRequest = PageRequest.of(current, size, parameter);
        PageResponse<StockDaily> pageResponse = stockDailyDAO.page(pageRequest);
        return WebPageResponse.ok(pageResponse);
    }
}
