package com.github.walkvoid.zone.finance.business.impl;

import com.github.walkvoid.zone.finance.api.service.StockMonthlyService;
import com.github.walkvoid.zone.finance.business.db.dao.StockMonthlyDAO;
import com.github.walkvoid.zone.finance.model.entity.StockMonthly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StockMonthlyServiceImpl implements StockMonthlyService {

    @Autowired
    private StockMonthlyDAO stockMonthlyDAO;

    @Override
    public StockMonthly getById(Long id) {
        return stockMonthlyDAO.selectById(id);
    }

    @Override
    public List<StockMonthly> listAll() {
        return stockMonthlyDAO.selectAll();
    }

    @Override
    public int insert(StockMonthly entity) {
        return stockMonthlyDAO.insert(entity);
    }

    @Override
    public int update(StockMonthly entity) {
        return stockMonthlyDAO.updateById(entity);
    }

    @Override
    public int delete(Long id) {
        return stockMonthlyDAO.deleteById(id);
    }
}
