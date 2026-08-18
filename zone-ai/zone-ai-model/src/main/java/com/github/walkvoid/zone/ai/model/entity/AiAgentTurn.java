package com.github.walkvoid.zone.ai.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Agent 一轮问答（用户问一次）。中间工具步骤在 {@link AiAgentStep}。
 */
@Data
@TableName("ai_agent_turn")
public class AiAgentTurn implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private String conversationId;
    private String sessionId;
    private String turnNo;
    private String messageId;
    private String botId;
    private String botCode;
    private String chatId;
    private String userId;
    private String channelType;
    private String userText;
    private Integer hasImage;
    private String finalAnswer;
    /** 0运行中 1成功 2失败 3超时 */
    private Integer status;
    private String errorMessage;
    private Integer toolCallCount;
    private Long durationMs;
    private LocalDateTime createTime;
    private LocalDateTime finishTime;
}
