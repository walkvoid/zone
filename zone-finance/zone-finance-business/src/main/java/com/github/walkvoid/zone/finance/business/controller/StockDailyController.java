package com.github.walkvoid.zone.finance.business.controller;

import com.github.walkvoid.zone.finance.api.service.StockDailyService;
import com.github.walkvoid.zone.finance.business.job.StockDailyJobHandler;
import com.github.walkvoid.zone.finance.model.entity.StockDaily;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/stock/daily")
public class StockDailyController {

    @Autowired
    private StockDailyService stockDailyService;

    @Autowired
    private StockDailyJobHandler stockDailyJobHandler;

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
    @GetMapping("/fetch")
    public List<StockDaily> fetch(@RequestParam String stockCode,
                                  @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                  @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return stockDailyService.fetchAndSaveDaily(stockCode, startDate, endDate);
    }
}
