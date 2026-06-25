package com.github.walkvoid.zone.finance.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.walkvoid.zone.finance.business.db.mapper.StockIndustryChainMapper;
import com.github.walkvoid.zone.finance.model.entity.StockIndustryChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 行业产业链 DAO
 *
 * @author walkvoid
 */
@Repository
public class StockIndustryChainDAO {

    @Autowired
    private StockIndustryChainMapper mapper;

    public int insert(StockIndustryChain entity) {
        return mapper.insert(entity);
    }

    public int updateById(StockIndustryChain entity) {
        return mapper.updateById(entity);
    }

    public int deleteById(Long id) {
        return mapper.deleteById(id);
    }

    public List<StockIndustryChain> selectByStock(String stockCode) {
        return mapper.selectList(new QueryWrapper<StockIndustryChain>()
                .eq("stock_code", stockCode)
                .eq("status", 1)
                .orderByAsc("relation_type"));
    }

    public List<StockIndustryChain> selectByStockAndType(String stockCode, String relationType) {
        return mapper.selectList(new QueryWrapper<StockIndustryChain>()
                .eq("stock_code", stockCode)
                .eq("relation_type", relationType)
                .eq("status", 1));
    }
}
