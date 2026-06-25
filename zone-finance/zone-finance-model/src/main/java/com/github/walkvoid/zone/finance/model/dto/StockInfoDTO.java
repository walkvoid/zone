package com.github.walkvoid.zone.finance.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class StockInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String stockCode;

    private String stockName;

    private String market;

    private LocalDate listingDate;

    private Long totalShares;

    private Long circulatingShares;

    private String industryCode;

    private Integer status;
}
