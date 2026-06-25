package com.github.walkvoid.zone.ai.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.walkvoid.zone.common.model.BooleanEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI模型实体
 *
 * @author walkvoid
 */
@Data
@TableName("ai_model")
public class AiModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    /** 模型编码 */
    private String modelCode;

    /** 模型名称 */
    private String modelName;

    /** 供应商 */
    private String provider;

    /** API 地址 */
    private String baseUrl;

    /** API 密钥 */
    private String apiKey;

    /** 累计调用次数 */
    private Long callCount;

    /** 是否启用 */
    private BooleanEnum isEnabled;

    /** 优先级 */
    private Integer priority;

    /** 描述 */
    private String description;

    /** 扩展配置 JSON */
    private String configJson;

    private Long createId;
    private LocalDateTime createTime;
    private Long updateId;
    private LocalDateTime updateTime;
}
