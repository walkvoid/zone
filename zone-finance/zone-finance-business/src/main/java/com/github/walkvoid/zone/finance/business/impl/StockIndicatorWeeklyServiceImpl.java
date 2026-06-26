package com.github.walkvoid.zone.finance.business.impl;

import com.github.walkvoid.zone.finance.api.service.StockIndicatorWeeklyService;
import com.github.walkvoid.zone.finance.business.db.dao.StockIndicatorWeeklyDAO;
import com.github.walkvoid.zone.finance.model.entity.StockIndicatorWeekly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StockIndicatorWeeklyServiceImpl implements StockIndicatorWeeklyService {

    @Autowired
    private StockIndicatorWeeklyDAO stockIndicatorWeeklyDAO;

    @Override
    public StockIndicatorWeekly getById(Long id) {
        return stockIndicatorWeeklyDAO.selectById(id);
    }

    @Override
    public List<StockIndicatorWeekly> listAll() {
        return stockIndicatorWeeklyDAO.selectAll();
    }

    @Override
    public int insert(StockIndicatorWeekly entity) {
        return stockIndicatorWeeklyDAO.insert(entity);
    }

    @Override
    public int update(StockIndicatorWeekly entity) {
        return stockIndicatorWeeklyDAO.updateById(entity);
    }

    @Override
    public int delete(Long id) {
        return stockIndicatorWeeklyDAO.deleteById(id);
    }
}
