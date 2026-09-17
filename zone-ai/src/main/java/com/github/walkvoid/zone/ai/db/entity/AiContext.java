package com.github.walkvoid.zone.ai.db.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.walkvoid.wvframework.models.BooleanEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 上下文
 */
@Data
@TableName("ai_context")
public class AiContext implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    /** 上下文名称 */
    private String name;

    /** 上下文路径 */
    private String contextPath;

    /** 注入对话的系统提示摘要 */
    private String systemHint;

    private String description;

    private BooleanEnum enabled;

    private Long createId;
    private LocalDateTime createTime;
    private Long updateId;
    private LocalDateTime updateTime;
}
