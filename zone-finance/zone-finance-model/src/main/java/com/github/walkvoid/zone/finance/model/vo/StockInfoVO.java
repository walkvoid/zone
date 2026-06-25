package com.github.walkvoid.zone.finance.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StockInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String stockCode;

    private String stockName;

    private String market;

    private LocalDate listingDate;

    private Long totalShares;

    private Long circulatingShares;

    private String industryCode;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
