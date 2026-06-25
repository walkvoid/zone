package com.github.walkvoid.zone.finance.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 选股策略实体
 *
 * @author walkvoid
 */
@Data
@TableName("stock_strategy")
public class StockStrategy implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    /** 策略编码 */
    private String strategyCode;

    /** 策略名称 */
    private String strategyName;

    /** 策略描述 */
    private String strategyDesc;

    /** 分类：TREND/MOMENTUM/VOLUME/PATTERN/BREAK */
    private String category;

    /** 策略执行日期 */
    private LocalDate execDate;

    /** 策略参数（JSON） */
    private String params;

    /** 状态：1-启用，0-禁用 */
    private Integer status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
