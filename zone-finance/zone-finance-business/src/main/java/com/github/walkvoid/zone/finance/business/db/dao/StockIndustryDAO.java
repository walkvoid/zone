package com.github.walkvoid.zone.finance.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.walkvoid.zone.finance.business.db.mapper.StockIndustryMapper;
import com.github.walkvoid.zone.finance.model.entity.StockIndustry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StockIndustryDAO {

    @Autowired
    private StockIndustryMapper mapper;

    public int insert(StockIndustry entity) { return mapper.insert(entity); }
    public int updateById(StockIndustry entity) { return mapper.updateById(entity); }
    public int deleteById(Long id) { return mapper.deleteById(id); }
    public StockIndustry selectById(Long id) { return mapper.selectById(id); }

    public StockIndustry selectByCode(String classification, String industryCode) {
        return mapper.selectOne(new QueryWrapper<StockIndustry>()
                .eq("classification", classification)
                .eq("industry_code", industryCode));
    }

    public List<StockIndustry> selectByClassification(String classification) {
        return mapper.selectList(new QueryWrapper<StockIndustry>()
                .eq("classification", classification)
                .orderByAsc("level", "industry_code"));
    }

    public List<StockIndustry> selectByParent(String parentCode) {
        return mapper.selectList(new QueryWrapper<StockIndustry>()
                .eq("parent_code", parentCode)
                .orderByAsc("industry_code"));
    }
}
