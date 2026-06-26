package com.github.walkvoid.zone.finance.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.walkvoid.zone.finance.business.db.mapper.StockInfoMapper;
import com.github.walkvoid.zone.finance.model.entity.StockInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StockInfoDAO {

    @Autowired
    private StockInfoMapper mapper;

    public int insert(StockInfo entity) { return mapper.insert(entity); }
    public int updateById(StockInfo entity) { return mapper.updateById(entity); }
    public int deleteById(Long id) { return mapper.deleteById(id); }
    public StockInfo selectById(Long id) { return mapper.selectById(id); }

    public IPage<StockInfo> selectPage(Page<StockInfo> page, QueryWrapper<StockInfo> wrapper) {
        return mapper.selectPage(page, wrapper);
    }

    public StockInfo selectByCode(String stockCode) {
        return mapper.selectOne(new QueryWrapper<StockInfo>().eq("stock_code", stockCode));
    }

    public List<StockInfo> selectList(StockInfo condition) {
        return mapper.selectList(new QueryWrapper<>(condition)
                .orderByAsc("stock_code"));
    }

    public List<StockInfo> selectByMarket(String market) {
        return mapper.selectList(new QueryWrapper<StockInfo>()
                .eq("market", market).eq("status", 1));
    }

    /** 总记录数 */
    public long countAll() {
        return mapper.selectCount(new QueryWrapper<>());
    }

    /** 按偏移量取一条（ORDER BY stock_code） */
    public StockInfo selectByOffset(int offset) {
        return mapper.selectList(new QueryWrapper<StockInfo>()
                .orderByAsc("stock_code")
                .last("LIMIT 1 OFFSET " + offset))
                .stream().findFirst().orElse(null);
    }
}
