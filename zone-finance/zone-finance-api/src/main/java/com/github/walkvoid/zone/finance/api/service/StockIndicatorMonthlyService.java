package com.github.walkvoid.zone.finance.api.service;

import com.github.walkvoid.zone.finance.model.entity.StockIndicatorMonthly;
import java.util.List;

public interface StockIndicatorMonthlyService {
    StockIndicatorMonthly getById(Long id);
    List<StockIndicatorMonthly> listAll();
    int insert(StockIndicatorMonthly entity);
    int update(StockIndicatorMonthly entity);
    int delete(Long id);
}
