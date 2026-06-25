package com.github.walkvoid.zone.finance.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.walkvoid.zone.finance.business.db.mapper.StockShareholderMapper;
import com.github.walkvoid.zone.finance.model.entity.StockShareholder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class StockShareholderDAO {

    @Autowired
    private StockShareholderMapper mapper;

    public int insert(StockShareholder entity) { return mapper.insert(entity); }
    public int updateById(StockShareholder entity) { return mapper.updateById(entity); }
    public int deleteById(Long id) { return mapper.deleteById(id); }
    public StockShareholder selectById(Long id) { return mapper.selectById(id); }

    public List<StockShareholder> selectByStockCode(String stockCode) {
        return mapper.selectList(new QueryWrapper<StockShareholder>()
                .eq("stock_code", stockCode)
                .orderByDesc("report_date", "hold_pct"));
    }

    public List<StockShareholder> selectByReportDate(String stockCode, LocalDate reportDate) {
        return mapper.selectList(new QueryWrapper<StockShareholder>()
                .eq("stock_code", stockCode)
                .eq("report_date", reportDate)
                .orderByDesc("hold_pct"));
    }
}
