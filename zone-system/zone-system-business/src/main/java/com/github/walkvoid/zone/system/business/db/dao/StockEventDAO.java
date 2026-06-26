package com.github.walkvoid.zone.system.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.walkvoid.zone.system.business.db.mapper.StockEventMapper;
import com.github.walkvoid.zone.system.model.entity.StockEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class StockEventDAO {

    @Autowired
    private StockEventMapper mapper;

    public StockEvent selectById(Long id) { return mapper.selectById(id); }
    public List<StockEvent> selectAll() { return mapper.selectList(new QueryWrapper<>()); }
    public int insert(StockEvent entity) { return mapper.insert(entity); }
    public int updateById(StockEvent entity) { return mapper.updateById(entity); }
    public int deleteById(Long id) { return mapper.deleteById(id); }
}
