package com.github.walkvoid.zone.finance.api.service;

import com.github.walkvoid.zone.finance.model.entity.StockStrategy;
import java.util.List;

public interface StockStrategyService {
    StockStrategy getById(Long id);
    List<StockStrategy> listAll();
    int insert(StockStrategy entity);
    int update(StockStrategy entity);
    int delete(Long id);
}
