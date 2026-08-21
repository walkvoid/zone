package com.github.walkvoid.zone.finance.service.impl;

import com.github.walkvoid.zone.finance.service.StockProductService;
import com.github.walkvoid.zone.finance.db.dao.StockProductDAO;
import com.github.walkvoid.zone.finance.db.entity.StockProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StockProductServiceImpl implements StockProductService {

    @Autowired
    private StockProductDAO stockProductDAO;

    @Override
    public StockProduct getById(Long id) {
        return stockProductDAO.selectById(id);
    }

    @Override
    public List<StockProduct> listAll() {
        return stockProductDAO.selectAll();
    }

    @Override
    public int insert(StockProduct entity) {
        return stockProductDAO.insert(entity);
    }

    @Override
    public int update(StockProduct entity) {
        return stockProductDAO.updateById(entity);
    }

    @Override
    public int delete(Long id) {
        return stockProductDAO.deleteById(id);
    }
}
