package com.github.walkvoid.zone.ai.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AiCodeChangeDTO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    private String conversationId;
    private String turnNo;
    private String messageId;
    private String botId;
    private String botCode;
    private String chatId;
    private String userId;
    private String channelType;
    private String title;
    private String requestText;
    private String writeMode;
    private Integer status;
    private Integer patchCount;
    private String applyMessage;
    private LocalDateTime applyTime;
    private LocalDateTime createTime;
    private List<AiCodeChangePatchDTO> patches;
}
