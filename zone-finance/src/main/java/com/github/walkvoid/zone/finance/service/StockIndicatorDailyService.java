package com.github.walkvoid.zone.finance.service;

import com.github.walkvoid.zone.finance.db.entity.StockIndicatorDaily;
import java.util.List;

public interface StockIndicatorDailyService {
    StockIndicatorDaily getById(Long id);
    List<StockIndicatorDaily> listAll();
    int insert(StockIndicatorDaily entity);
    int update(StockIndicatorDaily entity);
    int delete(Long id);
}
