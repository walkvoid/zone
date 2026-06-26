package com.github.walkvoid.zone.finance.business.impl;

import com.github.walkvoid.zone.finance.api.service.StockIntradayService;
import com.github.walkvoid.zone.finance.business.db.dao.StockIntradayDAO;
import com.github.walkvoid.zone.finance.model.entity.StockIntraday;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StockIntradayServiceImpl implements StockIntradayService {

    @Autowired
    private StockIntradayDAO stockIntradayDAO;

    @Override
    public StockIntraday getById(Long id) {
        return stockIntradayDAO.selectById(id);
    }

    @Override
    public List<StockIntraday> listAll() {
        return stockIntradayDAO.selectAll();
    }

    @Override
    public int insert(StockIntraday entity) {
        return stockIntradayDAO.insert(entity);
    }

    @Override
    public int update(StockIntraday entity) {
        return stockIntradayDAO.updateById(entity);
    }

    @Override
    public int delete(Long id) {
        return stockIntradayDAO.deleteById(id);
    }
}
