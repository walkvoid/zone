package com.github.walkvoid.zone.finance.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.walkvoid.zone.finance.business.db.mapper.StockIndicatorWeeklyMapper;
import com.github.walkvoid.zone.finance.model.entity.StockIndicatorWeekly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class StockIndicatorWeeklyDAO {

    @Autowired
    private StockIndicatorWeeklyMapper mapper;

    public int insert(StockIndicatorWeekly entity) { return mapper.insert(entity); }
    public int deleteById(Long id) { return mapper.deleteById(id); }

    public StockIndicatorWeekly selectByCodeAndDate(String stockCode, LocalDate tradeDate) {
        return mapper.selectOne(new QueryWrapper<StockIndicatorWeekly>()
                .eq("stock_code", stockCode).eq("trade_date", tradeDate));
    }

    public List<StockIndicatorWeekly> selectByCodeAndDateRange(String stockCode, LocalDate start, LocalDate end) {
        return mapper.selectList(new QueryWrapper<StockIndicatorWeekly>()
                .eq("stock_code", stockCode)
                .between("trade_date", start, end)
                .orderByAsc("trade_date"));
    }

    public List<StockIndicatorWeekly> selectLatest(String stockCode, int limit) {
        return mapper.selectList(new QueryWrapper<StockIndicatorWeekly>()
                .eq("stock_code", stockCode)
                .orderByDesc("trade_date")
                .last("LIMIT " + limit));
    }

    public StockIndicatorWeekly selectById(Long id) { return mapper.selectById(id); }
    public List<StockIndicatorWeekly> selectAll() { return mapper.selectList(new QueryWrapper<>()); }
    public int updateById(StockIndicatorWeekly entity) { return mapper.updateById(entity); }
}
