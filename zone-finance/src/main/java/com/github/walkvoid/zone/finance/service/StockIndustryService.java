package com.github.walkvoid.zone.finance.service;

import com.github.walkvoid.zone.finance.db.entity.StockIndustry;
import java.util.List;

public interface StockIndustryService {
    StockIndustry getById(Long id);
    List<StockIndustry> listAll();
    int insert(StockIndustry entity);
    int update(StockIndustry entity);
    int delete(Long id);
}
