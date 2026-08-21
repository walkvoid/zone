package com.github.walkvoid.zone.finance.service.impl;

import com.github.walkvoid.zone.finance.service.StockShareholderService;
import com.github.walkvoid.zone.finance.db.dao.StockShareholderDAO;
import com.github.walkvoid.zone.finance.db.entity.StockShareholder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StockShareholderServiceImpl implements StockShareholderService {

    @Autowired
    private StockShareholderDAO stockShareholderDAO;

    @Override
    public StockShareholder getById(Long id) {
        return stockShareholderDAO.selectById(id);
    }

    @Override
    public List<StockShareholder> listAll() {
        return stockShareholderDAO.selectAll();
    }

    @Override
    public int insert(StockShareholder entity) {
        return stockShareholderDAO.insert(entity);
    }

    @Override
    public int update(StockShareholder entity) {
        return stockShareholderDAO.updateById(entity);
    }

    @Override
    public int delete(Long id) {
        return stockShareholderDAO.deleteById(id);
    }
}
