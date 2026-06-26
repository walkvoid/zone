package com.github.walkvoid.zone.finance.business.impl;

import com.github.walkvoid.zone.finance.api.service.StockIndicatorDailyService;
import com.github.walkvoid.zone.finance.business.db.dao.StockIndicatorDailyDAO;
import com.github.walkvoid.zone.finance.model.entity.StockIndicatorDaily;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StockIndicatorDailyServiceImpl implements StockIndicatorDailyService {

    @Autowired
    private StockIndicatorDailyDAO stockIndicatorDailyDAO;

    @Override
    public StockIndicatorDaily getById(Long id) {
        return stockIndicatorDailyDAO.selectById(id);
    }

    @Override
    public List<StockIndicatorDaily> listAll() {
        return stockIndicatorDailyDAO.selectAll();
    }

    @Override
    public int insert(StockIndicatorDaily entity) {
        return stockIndicatorDailyDAO.insert(entity);
    }

    @Override
    public int update(StockIndicatorDaily entity) {
        return stockIndicatorDailyDAO.updateById(entity);
    }

    @Override
    public int delete(Long id) {
        return stockIndicatorDailyDAO.deleteById(id);
    }
}
