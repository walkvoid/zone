package com.github.walkvoid.zone.finance.api.service;

import com.github.walkvoid.zone.finance.model.entity.StockProduct;
import java.util.List;

public interface StockProductService {
    StockProduct getById(Long id);
    List<StockProduct> listAll();
    int insert(StockProduct entity);
    int update(StockProduct entity);
    int delete(Long id);
}
