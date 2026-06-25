package com.github.walkvoid.zone.finance.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("stock_shareholder")
public class StockShareholder implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;
    private String stockCode;
    private String holderName;
    private String holderType;
    private Long holdShares;
    private BigDecimal holdPct;
    private Long changeShares;
    private LocalDate reportDate;
    private Long createId;
    private LocalDateTime createTime;
    private Long updateId;
    private LocalDateTime updateTime;
}
