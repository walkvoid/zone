package com.github.walkvoid.zone.finance.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.walkvoid.zone.finance.business.db.mapper.StockWeeklyMapper;
import com.github.walkvoid.zone.finance.model.entity.StockWeekly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class StockWeeklyDAO {

    @Autowired
    private StockWeeklyMapper mapper;

    public int insert(StockWeekly entity) { return mapper.insert(entity); }
    public int deleteById(Long id) { return mapper.deleteById(id); }

    public StockWeekly selectByCodeAndDate(String stockCode, LocalDate tradeDate) {
        return mapper.selectOne(new QueryWrapper<StockWeekly>()
                .eq("stock_code", stockCode).eq("trade_date", tradeDate));
    }

    public List<StockWeekly> selectByCodeAndDateRange(String stockCode, LocalDate start, LocalDate end) {
        return mapper.selectList(new QueryWrapper<StockWeekly>()
                .eq("stock_code", stockCode)
                .between("trade_date", start, end)
                .orderByAsc("trade_date"));
    }
}
