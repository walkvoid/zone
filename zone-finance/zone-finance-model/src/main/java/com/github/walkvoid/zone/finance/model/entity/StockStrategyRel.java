package com.github.walkvoid.zone.finance.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 选股策略关联实体
 *
 * @author walkvoid
 */
@Data
@TableName("stock_strategy_rel")
public class StockStrategyRel implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    /** 策略ID */
    private Long strategyId;

    /** 股票代码 */
    private String stockCode;

    private LocalDateTime createTime;
}
