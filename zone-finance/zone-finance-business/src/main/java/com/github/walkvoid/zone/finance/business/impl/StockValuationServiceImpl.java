package com.github.walkvoid.zone.finance.business.impl;

import com.github.walkvoid.zone.finance.api.service.StockValuationService;
import com.github.walkvoid.zone.finance.business.db.dao.StockValuationDAO;
import com.github.walkvoid.zone.finance.model.entity.StockValuation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StockValuationServiceImpl implements StockValuationService {

    @Autowired
    private StockValuationDAO stockValuationDAO;

    @Override
    public StockValuation getById(Long id) {
        return stockValuationDAO.selectById(id);
    }

    @Override
    public List<StockValuation> listAll() {
        return stockValuationDAO.selectAll();
    }

    @Override
    public int insert(StockValuation entity) {
        return stockValuationDAO.insert(entity);
    }

    @Override
    public int update(StockValuation entity) {
        return stockValuationDAO.updateById(entity);
    }

    @Override
    public int delete(Long id) {
        return stockValuationDAO.deleteById(id);
    }
}
