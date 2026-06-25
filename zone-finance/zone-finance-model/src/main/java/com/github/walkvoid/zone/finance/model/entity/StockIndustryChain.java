package com.github.walkvoid.zone.finance.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 行业产业链实体
 *
 * @author walkvoid
 */
@Data
@TableName("stock_industry_chain")
public class StockIndustryChain implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private String stockCode;

    /** 关系：UPSTREAM-上游,DOWNSTREAM-下游,COMPETITOR-同行,SUBSTITUTE-替代 */
    private String relationType;

    /** 关联股票代码 */
    private String relatedStockCode;

    /** 关联公司名称 */
    private String relatedCompany;

    /** 关系描述 */
    private String relationDesc;

    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
