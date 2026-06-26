package com.github.walkvoid.zone.finance.business.impl;

import com.github.walkvoid.zone.finance.api.service.StockIndustryService;
import com.github.walkvoid.zone.finance.business.db.dao.StockIndustryDAO;
import com.github.walkvoid.zone.finance.model.entity.StockIndustry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StockIndustryServiceImpl implements StockIndustryService {

    @Autowired
    private StockIndustryDAO stockIndustryDAO;

    @Override
    public StockIndustry getById(Long id) {
        return stockIndustryDAO.selectById(id);
    }

    @Override
    public List<StockIndustry> listAll() {
        return stockIndustryDAO.selectAll();
    }

    @Override
    public int insert(StockIndustry entity) {
        return stockIndustryDAO.insert(entity);
    }

    @Override
    public int update(StockIndustry entity) {
        return stockIndustryDAO.updateById(entity);
    }

    @Override
    public int delete(Long id) {
        return stockIndustryDAO.deleteById(id);
    }
}
