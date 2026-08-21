package com.github.walkvoid.zone.finance.service;

import com.github.walkvoid.zone.finance.db.entity.StockIndicatorMonthly;
import java.util.List;

public interface StockIndicatorMonthlyService {
    StockIndicatorMonthly getById(Long id);
    List<StockIndicatorMonthly> listAll();
    int insert(StockIndicatorMonthly entity);
    int update(StockIndicatorMonthly entity);
    int delete(Long id);
}
