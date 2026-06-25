package com.github.walkvoid.zone.finance.api.service;

import com.github.walkvoid.zone.finance.model.dto.StockInfoDTO;
import com.github.walkvoid.zone.finance.model.query.StockInfoQuery;
import com.github.walkvoid.zone.finance.model.vo.StockInfoVO;

import java.util.Map;

/**
 * 股票信息 CRUD 服务接口
 *
 * @author walkvoid
 */
public interface StockInfoCrudService {

    /**
     * 分页查询股票列表
     */
    Map<String, Object> listPage(StockInfoQuery query);

    /**
     * 按ID查询详情
     */
    StockInfoVO getById(Long id);

    /**
     * 按股票代码查询
     */
    StockInfoVO getByCode(String stockCode);

    /**
     * 新增股票
     */
    void create(StockInfoDTO dto);

    /**
     * 更新股票
     */
    void update(Long id, StockInfoDTO dto);

    /**
     * 删除股票
     */
    void delete(Long id);
}
