package com.github.walkvoid.zone.finance.business.job;

import com.github.walkvoid.zone.finance.api.service.StockDailyService;
import com.github.walkvoid.zone.finance.business.db.dao.StockInfoDAO;
import com.github.walkvoid.zone.finance.model.entity.StockDaily;
import com.github.walkvoid.zone.finance.model.entity.StockInfo;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 股票日线数据定时更新Handler
 * 每5秒执行一次，每次处理1条股票，轮询更新
 *
 * @author walkvoid
 */
@Component
public class StockDailyJobHandler {

    private static final Logger log = LoggerFactory.getLogger(StockDailyJobHandler.class);

    private final AtomicLong cursor = new AtomicLong(0);

    private volatile long cachedTotal = 0;
    private volatile long lastRefreshTime = 0;
    private static final long REFRESH_INTERVAL_MS = 60 * 60 * 1000;

    @Autowired
    private StockInfoDAO stockInfoDAO;

    @Autowired
    private StockDailyService stockDailyService;

    @XxlJob("stockDailyJobHandler")
    public void execute() {
        long total = getTotalCount();
        if (total <= 0) {
            XxlJobHelper.handleSuccess("stock_info表为空");
            return;
        }

        long idx = cursor.getAndIncrement() % total;
        int index = (int) idx;

        StockInfo stock = stockInfoDAO.selectByOffset(index);
        if (stock == null) {
            refreshTotal();
            XxlJobHelper.handleSuccess("offset无数据");
            return;
        }

        String stockCode = stock.getStockCode();
        // 用昨天（大模型通常没有当天的实时数据）
        LocalDate tradeDate = getLastTradingDay();

        // 去重：已有数据跳过
        StockDaily existing = stockDailyService.getByCodeAndDate(stockCode, tradeDate);
        if (existing != null) {
            XxlJobHelper.handleSuccess("skip:" + stockCode);
            return;
        }

        log.info("获取日线: stock={}, date={}, cursor={}", stockCode, tradeDate, idx);
        try {
            List<StockDaily> result = stockDailyService.fetchAndSaveDaily(stockCode, tradeDate, tradeDate);
            if (result != null && !result.isEmpty()) {
                XxlJobHelper.handleSuccess("ok:" + stockCode);
            } else {
                // 大模型无数据，插入占位记录避免反复重试
                insertPlaceholder(stockCode, tradeDate);
                XxlJobHelper.handleSuccess("no_data:" + stockCode);
            }
        } catch (Exception e) {
            log.error("获取异常: stock={}, err={}", stockCode, e.getMessage());
            XxlJobHelper.handleFail("err:" + stockCode + " " + e.getMessage());
        }
    }

    /** 获取最近交易日（跳过周末） */
    private LocalDate getLastTradingDay() {
        LocalDate date = LocalDate.now().minusDays(1);
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.minusDays(1);
        }
        return date;
    }

    /** 插入占位记录避免反复重试同一只股票 */
    private void insertPlaceholder(String stockCode, LocalDate tradeDate) {
        try {
            StockDaily placeholder = new StockDaily();
            placeholder.setStockCode(stockCode);
            placeholder.setTradeDate(tradeDate);
            placeholder.setOpen(BigDecimal.ZERO);
            placeholder.setHigh(BigDecimal.ZERO);
            placeholder.setLow(BigDecimal.ZERO);
            placeholder.setClose(BigDecimal.ZERO);
            placeholder.setPreClose(BigDecimal.ZERO);
            placeholder.setVolume(0L);
            placeholder.setAmount(BigDecimal.ZERO);
            placeholder.setChangePct(BigDecimal.ZERO);
            placeholder.setTurnoverRate(BigDecimal.ZERO);
            placeholder.setAmplitude(BigDecimal.ZERO);
            stockDailyService.batchSave(List.of(placeholder));
            log.debug("占位: stock={}, date={}", stockCode, tradeDate);
        } catch (Exception e) {
            log.warn("占位失败: stock={}, err={}", stockCode, e.getMessage());
        }
    }

    private long getTotalCount() {
        long now = System.currentTimeMillis();
        if (cachedTotal <= 0 || (now - lastRefreshTime) > REFRESH_INTERVAL_MS) {
            cachedTotal = stockInfoDAO.countAll();
            lastRefreshTime = now;
            log.info("刷新股票总数: {}", cachedTotal);
        }
        return cachedTotal;
    }

    private void refreshTotal() {
        cachedTotal = stockInfoDAO.countAll();
        lastRefreshTime = System.currentTimeMillis();
    }
}
