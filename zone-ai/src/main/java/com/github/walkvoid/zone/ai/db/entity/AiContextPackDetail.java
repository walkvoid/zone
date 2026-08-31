package com.github.walkvoid.zone.ai.db.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 上下文包明细（一对多）
 */
@Data
@TableName("ai_context_pack_detail")
public class AiContextPackDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private Long packId;

    /** FILE / CODE / OTHER */
    private String detailType;

    private String title;

    private String content;

    /** 外部引用，如 file_info.id */
    private String refId;

    /** 扩展配置 JSON */
    private String configJson;

    private Integer sortOrder;

    private Long createId;
    private LocalDateTime createTime;
    private Long updateId;
    private LocalDateTime updateTime;
}
