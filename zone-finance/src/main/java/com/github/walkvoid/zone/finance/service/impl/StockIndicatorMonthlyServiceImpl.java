package com.github.walkvoid.zone.finance.service.impl;

import com.github.walkvoid.zone.finance.service.StockIndicatorMonthlyService;
import com.github.walkvoid.zone.finance.db.dao.StockIndicatorMonthlyDAO;
import com.github.walkvoid.zone.finance.db.entity.StockIndicatorMonthly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StockIndicatorMonthlyServiceImpl implements StockIndicatorMonthlyService {

    @Autowired
    private StockIndicatorMonthlyDAO stockIndicatorMonthlyDAO;

    @Override
    public StockIndicatorMonthly getById(Long id) {
        return stockIndicatorMonthlyDAO.selectById(id);
    }

    @Override
    public List<StockIndicatorMonthly> listAll() {
        return stockIndicatorMonthlyDAO.selectAll();
    }

    @Override
    public int insert(StockIndicatorMonthly entity) {
        return stockIndicatorMonthlyDAO.insert(entity);
    }

    @Override
    public int update(StockIndicatorMonthly entity) {
        return stockIndicatorMonthlyDAO.updateById(entity);
    }

    @Override
    public int delete(Long id) {
        return stockIndicatorMonthlyDAO.deleteById(id);
    }
}
