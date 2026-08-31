package com.github.walkvoid.zone.ai.db.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.walkvoid.wvframework.models.BooleanEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 上下文包
 */
@Data
@TableName("ai_context_pack")
public class AiContextPack implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    /** 编码，唯一 */
    private String packCode;

    /** 名称 */
    private String packName;

    /** 注入对话的系统提示摘要 */
    private String systemHint;

    private String description;

    private BooleanEnum isEnabled;

    private Long createId;
    private LocalDateTime createTime;
    private Long updateId;
    private LocalDateTime updateTime;
}
