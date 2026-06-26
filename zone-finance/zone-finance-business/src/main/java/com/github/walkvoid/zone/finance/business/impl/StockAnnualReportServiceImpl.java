package com.github.walkvoid.zone.finance.business.impl;

import com.github.walkvoid.zone.finance.api.service.StockAnnualReportService;
import com.github.walkvoid.zone.finance.business.db.dao.StockAnnualReportDAO;
import com.github.walkvoid.zone.finance.model.entity.StockAnnualReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StockAnnualReportServiceImpl implements StockAnnualReportService {

    @Autowired
    private StockAnnualReportDAO stockAnnualReportDAO;

    @Override
    public StockAnnualReport getById(Long id) {
        return stockAnnualReportDAO.selectById(id);
    }

    @Override
    public List<StockAnnualReport> listAll() {
        return stockAnnualReportDAO.selectAll();
    }

    @Override
    public int insert(StockAnnualReport entity) {
        return stockAnnualReportDAO.insert(entity);
    }

    @Override
    public int update(StockAnnualReport entity) {
        return stockAnnualReportDAO.updateById(entity);
    }

    @Override
    public int delete(Long id) {
        return stockAnnualReportDAO.deleteById(id);
    }
}
