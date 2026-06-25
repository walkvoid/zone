package com.github.walkvoid.zone.finance.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@TableName("stock_intraday")
public class StockIntraday implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;
    private String stockCode;
    private LocalDate tradeDate;
    private LocalTime tradeTime;
    private BigDecimal price;
    private Long volume;
    private BigDecimal amount;
    private BigDecimal avgPrice;
}
