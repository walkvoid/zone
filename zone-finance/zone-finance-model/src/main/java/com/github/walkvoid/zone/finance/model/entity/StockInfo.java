package com.github.walkvoid.zone.finance.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("stock_info")
public class StockInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
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
    private Long createId;
    private LocalDateTime createTime;
    private Long updateId;
    private LocalDateTime updateTime;
}
