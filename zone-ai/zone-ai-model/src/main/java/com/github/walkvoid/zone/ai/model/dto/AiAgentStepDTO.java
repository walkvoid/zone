package com.github.walkvoid.zone.ai.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiAgentStepDTO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    private String turnNo;
    private Integer seq;
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
