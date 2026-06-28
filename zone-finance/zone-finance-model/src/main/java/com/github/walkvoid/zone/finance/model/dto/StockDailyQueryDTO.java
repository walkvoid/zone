package com.github.walkvoid.zone.finance.model.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 日K线分页查询参数
 *
 * @author walkvoid
 */
@Data
public class StockDailyQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String stockCode;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}
