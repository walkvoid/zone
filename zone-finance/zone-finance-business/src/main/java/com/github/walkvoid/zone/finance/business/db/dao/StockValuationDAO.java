package com.github.walkvoid.zone.finance.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.walkvoid.zone.finance.business.db.mapper.StockValuationMapper;
import com.github.walkvoid.zone.finance.model.entity.StockValuation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 估值指标 DAO
 *
 * @author walkvoid
 */
@Repository
public class StockValuationDAO {

    @Autowired
    private StockValuationMapper mapper;

    public int insert(StockValuation entity) {
        return mapper.insert(entity);
    }

    public StockValuation selectLatest(String stockCode) {
        return mapper.selectOne(new QueryWrapper<StockValuation>()
                .eq("stock_code", stockCode)
                .orderByDesc("trade_date")
                .last("LIMIT 1"));
    }

    public List<StockValuation> selectByStock(String stockCode, LocalDate start, LocalDate end) {
        QueryWrapper<StockValuation> wrapper = new QueryWrapper<StockValuation>()
                .eq("stock_code", stockCode)
                .orderByAsc("trade_date");
        if (start != null) wrapper.ge("trade_date", start);
        if (end != null) wrapper.le("trade_date", end);
        return mapper.selectList(wrapper);
    }

    public StockValuation selectByStockAndDate(String stockCode, LocalDate tradeDate) {
        return mapper.selectOne(new QueryWrapper<StockValuation>()
                .eq("stock_code", stockCode)
                .eq("trade_date", tradeDate));
    }
}
