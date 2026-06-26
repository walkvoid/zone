package com.github.walkvoid.zone.system.api.service;

import com.github.walkvoid.zone.system.model.entity.StockEvent;
import java.util.List;

public interface StockEventService {
    StockEvent getById(Long id);
    List<StockEvent> listAll();
    int insert(StockEvent entity);
    int update(StockEvent entity);
    int delete(Long id);
}
