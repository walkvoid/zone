package com.github.walkvoid.zone.finance.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.walkvoid.zone.finance.business.db.mapper.StockAnnualReportMapper;
import com.github.walkvoid.zone.finance.model.entity.StockAnnualReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StockAnnualReportDAO {

    @Autowired
    private StockAnnualReportMapper mapper;

    public int insert(StockAnnualReport entity) { return mapper.insert(entity); }
    public int updateById(StockAnnualReport entity) { return mapper.updateById(entity); }
    public int deleteById(Long id) { return mapper.deleteById(id); }
    public StockAnnualReport selectById(Long id) { return mapper.selectById(id); }

    public StockAnnualReport selectByStockAndYear(String stockCode, int reportYear, String reportType) {
        return mapper.selectOne(new QueryWrapper<StockAnnualReport>()
                .eq("stock_code", stockCode)
                .eq("report_year", reportYear)
                .eq("report_type", reportType));
    }

    public List<StockAnnualReport> selectByStockCode(String stockCode) {
        return mapper.selectList(new QueryWrapper<StockAnnualReport>()
                .eq("stock_code", stockCode)
                .orderByDesc("report_year", "report_type"));
    }

    public List<StockAnnualReport> selectLatestYears(String stockCode, int limit) {
        return mapper.selectList(new QueryWrapper<StockAnnualReport>()
                .eq("stock_code", stockCode)
                .orderByDesc("report_year")
                .last("LIMIT " + limit));
    }
}
