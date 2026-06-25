package com.github.walkvoid.zone.finance.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("stock_indicator_daily")
public class StockIndicatorDaily implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;
    private String stockCode;
    private LocalDate tradeDate;
    private BigDecimal ma5;
    private BigDecimal ma10;
    private BigDecimal ma20;
    private BigDecimal ma60;
    private BigDecimal ma120;
    private BigDecimal ma250;
    private BigDecimal bollUpper;
    private BigDecimal bollMid;
    private BigDecimal bollLower;
    private BigDecimal macdDif;
    private BigDecimal macdDea;
    private BigDecimal macdHist;
    private BigDecimal kdjK;
    private BigDecimal kdjD;
    private BigDecimal kdjJ;
    private BigDecimal rsi6;
    private BigDecimal rsi12;
    private BigDecimal rsi24;
    private Long volMa5;
    private Long volMa10;
    private LocalDateTime createTime;
}
