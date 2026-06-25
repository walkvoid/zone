package com.github.walkvoid.zone.finance.model.query;

import lombok.Data;

import java.io.Serializable;

@Data
public class StockInfoQuery implements Serializable {
    private static final long serialVersionUID = 1L;

    private String stockCode;

    private String stockName;

    private String market;

    private Integer status;

    private Integer page = 1;

    private Integer size = 10;
}
