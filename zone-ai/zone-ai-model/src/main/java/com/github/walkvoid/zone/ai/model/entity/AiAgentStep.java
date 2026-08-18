package com.github.walkvoid.zone.ai.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 一轮问答中的一次工具调用（已截断、脱敏）。
 */
@Data
@TableName("ai_agent_step")
public class AiAgentStep implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private String turnNo;
    private Integer seq;
    /** TOOL */
    private String stepType;
    private String toolCode;
    private String toolName;
    private String requestJson;
    private String responseJson;
    private String responseSummary;
    private Integer success;
    private Long durationMs;
    private String errorMessage;
    private LocalDateTime createTime;
}
