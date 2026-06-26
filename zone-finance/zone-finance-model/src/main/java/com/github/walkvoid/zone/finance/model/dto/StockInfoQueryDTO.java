package com.github.walkvoid.zone.finance.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 股票查询参数 DTO
 *
 * @author walkvoid
 */
@Data
public class StockInfoQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String stockCode;

    private String stockName;

    private String market;

    private Integer status;

    private Integer page = 1;

    private Integer size = 10;
}
