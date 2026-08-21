package com.github.walkvoid.zone.finance.service;

import com.github.walkvoid.zone.finance.db.entity.StockStrategy;
import java.util.List;

public interface StockStrategyService {
    StockStrategy getById(Long id);
    List<StockStrategy> listAll();
    int insert(StockStrategy entity);
    int update(StockStrategy entity);
    int delete(Long id);
}
