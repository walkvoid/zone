package com.github.walkvoid.zone.finance.business.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.walkvoid.zone.finance.model.entity.StockStrategy;
import org.apache.ibatis.annotations.Mapper;

/**
 * 选股策略 Mapper
 *
 * @author walkvoid
 */
@Mapper
public interface StockStrategyMapper extends BaseMapper<StockStrategy> {
}
