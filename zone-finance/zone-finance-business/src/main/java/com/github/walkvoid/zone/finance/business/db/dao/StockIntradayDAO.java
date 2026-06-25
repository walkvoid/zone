package com.github.walkvoid.zone.finance.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.walkvoid.zone.finance.business.db.mapper.StockIntradayMapper;
import com.github.walkvoid.zone.finance.model.entity.StockIntraday;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class StockIntradayDAO {

    @Autowired
    private StockIntradayMapper mapper;

    public int insert(StockIntraday entity) { return mapper.insert(entity); }

    public List<StockIntraday> selectByCodeAndDate(String stockCode, LocalDate tradeDate) {
        return mapper.selectList(new QueryWrapper<StockIntraday>()
                .eq("stock_code", stockCode)
                .eq("trade_date", tradeDate)
                .orderByAsc("trade_time"));
    }
}
