package com.github.walkvoid.zone.finance.service;

import com.github.walkvoid.zone.finance.db.entity.StockShareholder;
import java.util.List;

public interface StockShareholderService {
    StockShareholder getById(Long id);
    List<StockShareholder> listAll();
    int insert(StockShareholder entity);
    int update(StockShareholder entity);
    int delete(Long id);
}
