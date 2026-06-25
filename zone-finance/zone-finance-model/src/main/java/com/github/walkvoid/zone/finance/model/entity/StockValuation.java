package com.github.walkvoid.zone.finance.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 估值指标实体
 *
 * @author walkvoid
 */
@Data
@TableName("stock_valuation")
public class StockValuation implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private String stockCode;
    private LocalDate tradeDate;

    /** 市盈率(TTM) */
    private BigDecimal peTtm;

    /** 市盈率(静态) */
    private BigDecimal peStatic;

    /** 市盈率(动态) */
    private BigDecimal peDynamic;

    /** 市净率 */
    private BigDecimal pb;

    /** 市销率 */
    private BigDecimal ps;

    /** 总市值(亿) */
    private BigDecimal totalMv;

    /** 流通市值(亿) */
    private BigDecimal circulatingMv;

    /** 股息率(%) */
    private BigDecimal divYield;

    /** PE历史分位(%) */
    private BigDecimal pePercentile;

    /** PB历史分位(%) */
    private BigDecimal pbPercentile;

    private LocalDateTime createTime;
}
