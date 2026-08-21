package com.github.walkvoid.zone.finance.service;

import com.github.walkvoid.zone.finance.db.entity.StockMonthly;
import java.util.List;

public interface StockMonthlyService {
    StockMonthly getById(Long id);
    List<StockMonthly> listAll();
    int insert(StockMonthly entity);
    int update(StockMonthly entity);
    int delete(Long id);
}
