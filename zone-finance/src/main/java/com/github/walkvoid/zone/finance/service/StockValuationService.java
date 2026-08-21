package com.github.walkvoid.zone.finance.service;

import com.github.walkvoid.zone.finance.db.entity.StockValuation;
import java.util.List;

public interface StockValuationService {
    StockValuation getById(Long id);
    List<StockValuation> listAll();
    int insert(StockValuation entity);
    int update(StockValuation entity);
    int delete(Long id);
}
