package com.github.walkvoid.zone.finance.service;

import com.github.walkvoid.zone.finance.db.entity.StockProduct;
import java.util.List;

public interface StockProductService {
    StockProduct getById(Long id);
    List<StockProduct> listAll();
    int insert(StockProduct entity);
    int update(StockProduct entity);
    int delete(Long id);
}
