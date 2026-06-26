package com.github.walkvoid.zone.finance.api.service;

import com.github.walkvoid.zone.finance.model.entity.StockShareholder;
import java.util.List;

public interface StockShareholderService {
    StockShareholder getById(Long id);
    List<StockShareholder> listAll();
    int insert(StockShareholder entity);
    int update(StockShareholder entity);
    int delete(Long id);
}
