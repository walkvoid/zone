package com.github.walkvoid.zone.system.service.impl;

import com.github.walkvoid.zone.system.service.StockEventService;
import com.github.walkvoid.zone.system.db.dao.StockEventDAO;
import com.github.walkvoid.zone.system.db.entity.StockEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StockEventServiceImpl implements StockEventService {

    @Autowired
    private StockEventDAO stockEventDAO;

    @Override
    public StockEvent getById(Long id) {
        return stockEventDAO.selectById(id);
    }

    @Override
    public List<StockEvent> listAll() {
        return stockEventDAO.selectAll();
    }

    @Override
    public int insert(StockEvent entity) {
        return stockEventDAO.insert(entity);
    }

    @Override
    public int update(StockEvent entity) {
        return stockEventDAO.updateById(entity);
    }

    @Override
    public int delete(Long id) {
        return stockEventDAO.deleteById(id);
    }
}
