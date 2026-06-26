package com.github.walkvoid.zone.finance.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 股票信息 DTO（表单 + 响应共用）
 *
 * @author walkvoid
 */
@Data
public class StockInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String stockCode;

    private String stockName;

    private String market;

    private Integer plate;

    private LocalDate listingDate;

    private Long totalShares;

    private Long circulatingShares;

    private String industryCode;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
