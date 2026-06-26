package com.github.walkvoid.zone.finance.business.impl;

import com.github.walkvoid.zone.finance.api.service.StockWeeklyService;
import com.github.walkvoid.zone.finance.business.db.dao.StockWeeklyDAO;
import com.github.walkvoid.zone.finance.model.entity.StockWeekly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StockWeeklyServiceImpl implements StockWeeklyService {

    @Autowired
    private StockWeeklyDAO stockWeeklyDAO;

    @Override
    public StockWeekly getById(Long id) {
        return stockWeeklyDAO.selectById(id);
    }

    @Override
    public List<StockWeekly> listAll() {
        return stockWeeklyDAO.selectAll();
    }

    @Override
    public int insert(StockWeekly entity) {
        return stockWeeklyDAO.insert(entity);
    }

    @Override
    public int update(StockWeekly entity) {
        return stockWeeklyDAO.updateById(entity);
    }

    @Override
    public int delete(Long id) {
        return stockWeeklyDAO.deleteById(id);
    }
}
