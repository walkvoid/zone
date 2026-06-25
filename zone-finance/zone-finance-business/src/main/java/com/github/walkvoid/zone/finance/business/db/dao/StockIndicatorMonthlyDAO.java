package com.github.walkvoid.zone.finance.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.walkvoid.zone.finance.business.db.mapper.StockIndicatorMonthlyMapper;
import com.github.walkvoid.zone.finance.model.entity.StockIndicatorMonthly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class StockIndicatorMonthlyDAO {

    @Autowired
    private StockIndicatorMonthlyMapper mapper;

    public int insert(StockIndicatorMonthly entity) { return mapper.insert(entity); }
    public int deleteById(Long id) { return mapper.deleteById(id); }

    public StockIndicatorMonthly selectByCodeAndDate(String stockCode, LocalDate tradeDate) {
        return mapper.selectOne(new QueryWrapper<StockIndicatorMonthly>()
                .eq("stock_code", stockCode).eq("trade_date", tradeDate));
    }

    public List<StockIndicatorMonthly> selectByCodeAndDateRange(String stockCode, LocalDate start, LocalDate end) {
        return mapper.selectList(new QueryWrapper<StockIndicatorMonthly>()
                .eq("stock_code", stockCode)
                .between("trade_date", start, end)
                .orderByAsc("trade_date"));
    }

    public List<StockIndicatorMonthly> selectLatest(String stockCode, int limit) {
        return mapper.selectList(new QueryWrapper<StockIndicatorMonthly>()
                .eq("stock_code", stockCode)
                .orderByDesc("trade_date")
                .last("LIMIT " + limit));
    }
}
