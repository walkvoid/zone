package com.github.walkvoid.zone.finance.business.impl;

import com.github.walkvoid.zone.finance.api.service.StockStrategyService;
import com.github.walkvoid.zone.finance.business.db.dao.StockStrategyDAO;
import com.github.walkvoid.zone.finance.model.entity.StockStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StockStrategyServiceImpl implements StockStrategyService {

    @Autowired
    private StockStrategyDAO stockStrategyDAO;

    @Override
    public StockStrategy getById(Long id) {
        return stockStrategyDAO.selectById(id);
    }

    @Override
    public List<StockStrategy> listAll() {
        return stockStrategyDAO.selectAll(null);
    }

    @Override
    public int insert(StockStrategy entity) {
        return stockStrategyDAO.insert(entity);
    }

    @Override
    public int update(StockStrategy entity) {
        return stockStrategyDAO.updateById(entity);
    }

    @Override
    public int delete(Long id) {
        return stockStrategyDAO.deleteById(id);
    }
}
