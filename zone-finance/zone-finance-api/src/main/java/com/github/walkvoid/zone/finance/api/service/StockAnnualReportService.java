package com.github.walkvoid.zone.finance.api.service;

import com.github.walkvoid.zone.finance.model.entity.StockAnnualReport;
import java.util.List;

public interface StockAnnualReportService {
    StockAnnualReport getById(Long id);
    List<StockAnnualReport> listAll();
    int insert(StockAnnualReport entity);
    int update(StockAnnualReport entity);
    int delete(Long id);
}
