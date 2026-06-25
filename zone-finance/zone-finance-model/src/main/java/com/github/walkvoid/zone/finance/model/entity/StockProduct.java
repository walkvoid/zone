package com.github.walkvoid.zone.finance.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 公司产品/业务实体
 *
 * @author walkvoid
 */
@Data
@TableName("stock_product")
public class StockProduct implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private String stockCode;

    /** 产品/业务名称 */
    private String productName;

    /** 类型：MAIN-主营,NEW-新业务,OTHER-其他 */
    private String productType;

    /** 营收占比(%) */
    private BigDecimal revenueRatio;

    /** 毛利率(%) */
    private BigDecimal grossMargin;

    /** 产品描述 */
    private String description;

    /** 报告年份 */
    private Integer reportYear;

    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
