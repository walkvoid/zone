package com.github.walkvoid.zone.finance.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.walkvoid.zone.finance.business.db.mapper.StockFundRelMapper;
import com.github.walkvoid.zone.finance.model.entity.StockFundRel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class StockFundRelDAO {

    @Autowired
    private StockFundRelMapper mapper;

    public int insert(StockFundRel entity) { return mapper.insert(entity); }
    public int deleteById(Long id) { return mapper.deleteById(id); }

    public List<StockFundRel> selectByStockCode(String stockCode) {
        return mapper.selectList(new QueryWrapper<StockFundRel>()
                .eq("stock_code", stockCode)
                .orderByDesc("report_date", "hold_pct"));
    }

    public List<StockFundRel> selectByFundCode(String fundCode) {
        return mapper.selectList(new QueryWrapper<StockFundRel>()
                .eq("fund_code", fundCode)
                .orderByDesc("report_date"));
    }

    public List<StockFundRel> selectByStockAndReport(String stockCode, LocalDate reportDate) {
        return mapper.selectList(new QueryWrapper<StockFundRel>()
                .eq("stock_code", stockCode)
                .eq("report_date", reportDate)
                .orderByDesc("hold_pct"));
    }
}
