package com.github.walkvoid.zone.finance.api.service;

import com.github.walkvoid.zone.finance.model.entity.FundInfo;
import java.util.List;

public interface FundInfoService {
    FundInfo getById(Long id);
    List<FundInfo> listAll();
    int insert(FundInfo entity);
    int update(FundInfo entity);
    int delete(Long id);
}
