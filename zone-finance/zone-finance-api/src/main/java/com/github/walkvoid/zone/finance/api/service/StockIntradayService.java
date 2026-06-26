package com.github.walkvoid.zone.finance.api.service;

import com.github.walkvoid.zone.finance.model.entity.StockIntraday;
import java.util.List;

public interface StockIntradayService {
    StockIntraday getById(Long id);
    List<StockIntraday> listAll();
    int insert(StockIntraday entity);
    int update(StockIntraday entity);
    int delete(Long id);
}
