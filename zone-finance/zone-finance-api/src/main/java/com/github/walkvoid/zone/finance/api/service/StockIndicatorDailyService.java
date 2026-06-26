package com.github.walkvoid.zone.finance.api.service;

import com.github.walkvoid.zone.finance.model.entity.StockIndicatorDaily;
import java.util.List;

public interface StockIndicatorDailyService {
    StockIndicatorDaily getById(Long id);
    List<StockIndicatorDaily> listAll();
    int insert(StockIndicatorDaily entity);
    int update(StockIndicatorDaily entity);
    int delete(Long id);
}
