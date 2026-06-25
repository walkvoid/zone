package com.github.walkvoid.zone.finance.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("stock_monthly")
public class StockMonthly implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;
    private String stockCode;
    private LocalDate tradeDate;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private Long volume;
    private BigDecimal amount;
    private BigDecimal changePct;
    private BigDecimal turnoverRate;
    private BigDecimal amplitude;
    private LocalDateTime createTime;
}
