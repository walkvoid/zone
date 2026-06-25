package com.github.walkvoid.zone.finance.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.walkvoid.zone.finance.business.db.mapper.StockStrategyMapper;
import com.github.walkvoid.zone.finance.model.entity.StockStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 选股策略 DAO
 *
 * @author walkvoid
 */
@Repository
public class StockStrategyDAO {

    @Autowired
    private StockStrategyMapper mapper;

    public int insert(StockStrategy entity) {
        return mapper.insert(entity);
    }

    public int updateById(StockStrategy entity) {
        return mapper.updateById(entity);
    }

    public int deleteById(Long id) {
        return mapper.deleteById(id);
    }

    public StockStrategy selectById(Long id) {
        return mapper.selectById(id);
    }

    public StockStrategy selectByCode(String strategyCode) {
        return mapper.selectOne(new QueryWrapper<StockStrategy>()
                .eq("strategy_code", strategyCode));
    }

    public List<StockStrategy> selectByCategory(String category) {
        return mapper.selectList(new QueryWrapper<StockStrategy>()
                .eq("category", category)
                .eq("status", 1));
    }

    public List<StockStrategy> selectByExecDate(LocalDate execDate) {
        return mapper.selectList(new QueryWrapper<StockStrategy>()
                .eq("exec_date", execDate)
                .eq("status", 1)
                .orderByAsc("category", "id"));
    }

    public List<StockStrategy> selectAll(Integer status) {
        QueryWrapper<StockStrategy> wrapper = new QueryWrapper<>();
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByAsc("category", "id");
        return mapper.selectList(wrapper);
    }

    public IPage<StockStrategy> selectPage(Page<StockStrategy> page, QueryWrapper<StockStrategy> wrapper) {
        return mapper.selectPage(page, wrapper);
    }
}
