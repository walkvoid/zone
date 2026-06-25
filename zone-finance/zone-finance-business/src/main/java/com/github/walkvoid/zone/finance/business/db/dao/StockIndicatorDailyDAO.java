package com.github.walkvoid.zone.finance.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.walkvoid.zone.finance.business.db.mapper.StockIndicatorDailyMapper;
import com.github.walkvoid.zone.finance.model.entity.StockIndicatorDaily;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class StockIndicatorDailyDAO {

    @Autowired
    private StockIndicatorDailyMapper mapper;

    public int insert(StockIndicatorDaily entity) { return mapper.insert(entity); }
    public int deleteById(Long id) { return mapper.deleteById(id); }

    public StockIndicatorDaily selectByCodeAndDate(String stockCode, LocalDate tradeDate) {
        return mapper.selectOne(new QueryWrapper<StockIndicatorDaily>()
                .eq("stock_code", stockCode).eq("trade_date", tradeDate));
    }

    public List<StockIndicatorDaily> selectByCodeAndDateRange(String stockCode, LocalDate start, LocalDate end) {
        return mapper.selectList(new QueryWrapper<StockIndicatorDaily>()
                .eq("stock_code", stockCode)
                .between("trade_date", start, end)
                .orderByAsc("trade_date"));
    }

    public List<StockIndicatorDaily> selectLatest(String stockCode, int limit) {
        return mapper.selectList(new QueryWrapper<StockIndicatorDaily>()
                .eq("stock_code", stockCode)
                .orderByDesc("trade_date")
                .last("LIMIT " + limit));
    }
}
