package com.github.walkvoid.zone.finance.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.walkvoid.zone.finance.business.db.mapper.StockStrategyRelMapper;
import com.github.walkvoid.zone.finance.model.entity.StockStrategyRel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 选股策略关联 DAO
 *
 * @author walkvoid
 */
@Repository
public class StockStrategyRelDAO {

    @Autowired
    private StockStrategyRelMapper mapper;

    public int insert(StockStrategyRel entity) {
        return mapper.insert(entity);
    }

    public int deleteById(Long id) {
        return mapper.deleteById(id);
    }

    public int deleteByStrategyId(Long strategyId) {
        return mapper.delete(new QueryWrapper<StockStrategyRel>()
                .eq("strategy_id", strategyId));
    }

    public List<StockStrategyRel> selectByStrategyId(Long strategyId) {
        return mapper.selectList(new QueryWrapper<StockStrategyRel>()
                .eq("strategy_id", strategyId)
                .orderByAsc("id"));
    }

    public List<StockStrategyRel> selectByStockCode(String stockCode) {
        return mapper.selectList(new QueryWrapper<StockStrategyRel>()
                .eq("stock_code", stockCode)
                .orderByAsc("id"));
    }
}
