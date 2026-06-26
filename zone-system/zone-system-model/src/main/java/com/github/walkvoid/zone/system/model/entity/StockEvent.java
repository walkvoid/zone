package com.github.walkvoid.zone.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("stock_event")
public class StockEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;
    private String stockCode;
    private String eventType;
    private LocalDate eventDate;
    private LocalDate announceDate;
    private String eventTitle;
    private String eventContent;
    private String source;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String extData;
}
