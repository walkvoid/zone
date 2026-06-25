package com.github.walkvoid.zone.finance.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("fund_info")
public class FundInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;
    private String fundCode;
    private String fundName;
    private String fundType;
    private String manager;
    private String company;
    private BigDecimal nav;
    private BigDecimal totalShare;
    private LocalDate establishDate;
    private Long createId;
    private LocalDateTime createTime;
    private Long updateId;
    private LocalDateTime updateTime;
}
