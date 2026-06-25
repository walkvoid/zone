package com.github.walkvoid.zone.finance.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("stock_industry")
public class StockIndustry implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;
    private String industryCode;
    private String industryName;
    private String classification;
    private String parentCode;
    private Integer level;
    private Long createId;
    private LocalDateTime createTime;
    private Long updateId;
    private LocalDateTime updateTime;
}
