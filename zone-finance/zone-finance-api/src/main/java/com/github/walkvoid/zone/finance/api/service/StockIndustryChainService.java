package com.github.walkvoid.zone.finance.api.service;

import com.github.walkvoid.zone.finance.model.entity.StockIndustryChain;
import java.util.List;

public interface StockIndustryChainService {
    StockIndustryChain getById(Long id);
    List<StockIndustryChain> listAll();
    int insert(StockIndustryChain entity);
    int update(StockIndustryChain entity);
    int delete(Long id);
}
