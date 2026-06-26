package com.github.walkvoid.zone.finance.business.impl;

import com.github.walkvoid.zone.finance.api.service.StockProductService;
import com.github.walkvoid.zone.finance.business.db.dao.StockProductDAO;
import com.github.walkvoid.zone.finance.model.entity.StockProduct;
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
