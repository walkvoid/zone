package com.github.walkvoid.zone.finance.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("stock_annual_report")
public class StockAnnualReport implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;
    private String stockCode;
    private Integer reportYear;
    private String reportType;
    private BigDecimal revenue;
    private BigDecimal revenueYoy;
    private BigDecimal netProfit;
    private BigDecimal netProfitYoy;
    private BigDecimal deductedProfit;
    private BigDecimal eps;
    private BigDecimal bvps;
    private BigDecimal roe;
    private BigDecimal roa;
    private BigDecimal grossMargin;
    private BigDecimal netMargin;
    private BigDecimal totalAssets;
    private BigDecimal totalLiabilities;
    private BigDecimal debtRatio;
    private Long createId;
    private LocalDateTime createTime;
    private Long updateId;
    private LocalDateTime updateTime;
}
