package com.github.walkvoid.zone.finance.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.walkvoid.zone.finance.business.db.mapper.StockDailyMapper;
import com.github.walkvoid.zone.finance.model.entity.StockDaily;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class StockDailyDAO {

    @Autowired
    private StockDailyMapper mapper;

    public int insert(StockDaily entity) { return mapper.insert(entity); }
    public int deleteById(Long id) { return mapper.deleteById(id); }

    public StockDaily selectByCodeAndDate(String stockCode, LocalDate tradeDate) {
        return mapper.selectOne(new QueryWrapper<StockDaily>()
                .eq("stock_code", stockCode).eq("trade_date", tradeDate));
    }

    public List<StockDaily> selectByCodeAndDateRange(String stockCode, LocalDate start, LocalDate end) {
        return mapper.selectList(new QueryWrapper<StockDaily>()
                .eq("stock_code", stockCode)
                .between("trade_date", start, end)
                .orderByAsc("trade_date"));
    }

    public StockDaily selectLatest(String stockCode) {
        return mapper.selectOne(new QueryWrapper<StockDaily>()
                .eq("stock_code", stockCode)
                .orderByDesc("trade_date")
                .last("LIMIT 1"));
    }
}
