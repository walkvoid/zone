package com.github.walkvoid.zone.finance.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.wvframework.models.PageResponse;
import com.github.walkvoid.zone.finance.business.db.mapper.StockDailyMapper;
import com.github.walkvoid.zone.finance.model.dto.StockDailyQueryDTO;
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

    public PageDTO<StockDaily> selectPage(PageDTO<StockDaily> pageDTO, String stockCode, LocalDate startDate, LocalDate endDate) {
        return mapper.selectPage(pageDTO, buildWrapper(stockCode, startDate, endDate));
    }

    public PageResponse<StockDaily> page(PageRequest<StockDailyQueryDTO> pageRequest) {
        StockDailyQueryDTO query = pageRequest.getParameter();
        String stockCode = query != null ? query.getStockCode() : null;
        LocalDate startDate = query != null ? query.getStartDate() : null;
        LocalDate endDate = query != null ? query.getEndDate() : null;
        Page<StockDaily> page = mapper.selectPage(
                new Page<>(pageRequest.getCurrent(), pageRequest.getSize()),
                buildWrapper(stockCode, startDate, endDate));
        return new PageResponse<>(page.getTotal(), (int) page.getSize(), page.getCurrent(), page.getRecords());
    }

    private QueryWrapper<StockDaily> buildWrapper(String stockCode, LocalDate startDate, LocalDate endDate) {
        QueryWrapper<StockDaily> qw = new QueryWrapper<>();
        if (stockCode != null && !stockCode.isBlank()) {
            qw.eq("stock_code", stockCode);
        }
        if (startDate != null && endDate != null) {
            qw.between("trade_date", startDate, endDate);
        }
        qw.orderByDesc("trade_date");
        return qw;
    }
}
