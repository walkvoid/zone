package com.github.walkvoid.zone.finance.business.impl;

import com.github.walkvoid.zone.finance.api.service.FundInfoService;
import com.github.walkvoid.zone.finance.business.db.dao.FundInfoDAO;
import com.github.walkvoid.zone.finance.model.entity.FundInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FundInfoServiceImpl implements FundInfoService {

    @Autowired
    private FundInfoDAO fundInfoDAO;

    @Override
    public FundInfo getById(Long id) {
        return fundInfoDAO.selectById(id);
    }

    @Override
    public List<FundInfo> listAll() {
        return fundInfoDAO.selectAll();
    }

    @Override
    public int insert(FundInfo entity) {
        return fundInfoDAO.insert(entity);
    }

    @Override
    public int update(FundInfo entity) {
        return fundInfoDAO.updateById(entity);
    }

    @Override
    public int delete(Long id) {
        return fundInfoDAO.deleteById(id);
    }
}
