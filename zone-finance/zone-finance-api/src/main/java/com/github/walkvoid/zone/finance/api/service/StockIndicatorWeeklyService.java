package com.github.walkvoid.zone.finance.api.service;

import com.github.walkvoid.zone.finance.model.entity.StockIndicatorWeekly;
import java.util.List;

public interface StockIndicatorWeeklyService {
    StockIndicatorWeekly getById(Long id);
    List<StockIndicatorWeekly> listAll();
    int insert(StockIndicatorWeekly entity);
    int update(StockIndicatorWeekly entity);
    int delete(Long id);
}
