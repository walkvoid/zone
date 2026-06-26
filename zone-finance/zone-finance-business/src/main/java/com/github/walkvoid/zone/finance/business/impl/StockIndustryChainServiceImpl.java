package com.github.walkvoid.zone.finance.business.impl;

import com.github.walkvoid.zone.finance.api.service.StockIndustryChainService;
import com.github.walkvoid.zone.finance.business.db.dao.StockIndustryChainDAO;
import com.github.walkvoid.zone.finance.model.entity.StockIndustryChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StockIndustryChainServiceImpl implements StockIndustryChainService {

    @Autowired
    private StockIndustryChainDAO stockIndustryChainDAO;

    @Override
    public StockIndustryChain getById(Long id) {
        return stockIndustryChainDAO.selectById(id);
    }

    @Override
    public List<StockIndustryChain> listAll() {
        return stockIndustryChainDAO.selectAll();
    }

    @Override
    public int insert(StockIndustryChain entity) {
        return stockIndustryChainDAO.insert(entity);
    }

    @Override
    public int update(StockIndustryChain entity) {
        return stockIndustryChainDAO.updateById(entity);
    }

    @Override
    public int delete(Long id) {
        return stockIndustryChainDAO.deleteById(id);
    }
}
