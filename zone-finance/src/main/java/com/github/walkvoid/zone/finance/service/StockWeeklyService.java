package com.github.walkvoid.zone.finance.service;

import com.github.walkvoid.zone.finance.db.entity.StockWeekly;
import java.util.List;

public interface StockWeeklyService {
    StockWeekly getById(Long id);
    List<StockWeekly> listAll();
    int insert(StockWeekly entity);
    int update(StockWeekly entity);
    int delete(Long id);
}
