package com.github.walkvoid.zone.finance.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.walkvoid.zone.finance.business.db.mapper.StockProductMapper;
import com.github.walkvoid.zone.finance.model.entity.StockProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 公司产品/业务 DAO
 *
 * @author walkvoid
 */
@Repository
public class StockProductDAO {

    @Autowired
    private StockProductMapper mapper;

    public int insert(StockProduct entity) {
        return mapper.insert(entity);
    }

    public int updateById(StockProduct entity) {
        return mapper.updateById(entity);
    }

    public int deleteById(Long id) {
        return mapper.deleteById(id);
    }

    public List<StockProduct> selectByStock(String stockCode) {
        return mapper.selectList(new QueryWrapper<StockProduct>()
                .eq("stock_code", stockCode)
                .eq("status", 1)
                .orderByDesc("revenue_ratio"));
    }

    public List<StockProduct> selectByStockAndYear(String stockCode, Integer reportYear) {
        return mapper.selectList(new QueryWrapper<StockProduct>()
                .eq("stock_code", stockCode)
                .eq("report_year", reportYear)
                .eq("status", 1)
                .orderByDesc("revenue_ratio"));
    }

    public StockProduct selectById(Long id) { return mapper.selectById(id); }
    public List<StockProduct> selectAll() { return mapper.selectList(new QueryWrapper<>()); }
}
