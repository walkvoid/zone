package com.github.walkvoid.zone.finance.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.walkvoid.zone.finance.business.db.mapper.FundInfoMapper;
import com.github.walkvoid.zone.finance.model.entity.FundInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FundInfoDAO {

    @Autowired
    private FundInfoMapper mapper;

    public int insert(FundInfo entity) { return mapper.insert(entity); }
    public int updateById(FundInfo entity) { return mapper.updateById(entity); }
    public int deleteById(Long id) { return mapper.deleteById(id); }
    public FundInfo selectById(Long id) { return mapper.selectById(id); }

    public FundInfo selectByCode(String fundCode) {
        return mapper.selectOne(new QueryWrapper<FundInfo>().eq("fund_code", fundCode));
    }

    public List<FundInfo> selectByCompany(String company) {
        return mapper.selectList(new QueryWrapper<FundInfo>()
                .eq("company", company).orderByAsc("fund_code"));
    }

    public List<FundInfo> selectList(FundInfo condition) {
        return mapper.selectList(new QueryWrapper<>(condition).orderByAsc("fund_code"));
    }
}
