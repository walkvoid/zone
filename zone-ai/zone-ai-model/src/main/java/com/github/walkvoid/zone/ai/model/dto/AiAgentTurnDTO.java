package com.github.walkvoid.zone.ai.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AiAgentTurnDTO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
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
    private Integer status;
    private String errorMessage;
    private Integer toolCallCount;
    private Long durationMs;
    private LocalDateTime createTime;
    private LocalDateTime finishTime;
    private List<AiAgentStepDTO> steps;
}
