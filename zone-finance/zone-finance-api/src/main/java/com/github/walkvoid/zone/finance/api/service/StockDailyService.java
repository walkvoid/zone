package com.github.walkvoid.zone.finance.api.service;

import com.github.walkvoid.zone.finance.model.entity.StockDaily;

import java.time.LocalDate;
import java.util.List;

public interface StockDailyService {

    StockDaily getByCodeAndDate(String stockCode, LocalDate tradeDate);

    List<StockDaily> listByDateRange(String stockCode, LocalDate start, LocalDate end);

    StockDaily getLatest(String stockCode);

    int batchSave(List<StockDaily> list);

    /**
     * 调用大模型获取日K线数据并保存
     */
    List<StockDaily> fetchAndSaveDaily(String stockCode, LocalDate startDate, LocalDate endDate);
}
