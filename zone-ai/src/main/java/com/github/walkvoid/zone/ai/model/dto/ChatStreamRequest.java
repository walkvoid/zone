package com.github.walkvoid.zone.ai.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 流式聊天请求
 */
@Data
public class ChatStreamRequest {

    /**
     * 可选。不传则取启用模型中优先级最高的一条。
     */
    private String modelCode;

    /**
     * 可选系统提示；若 messages 里已有 system，可不再传。
     */
    private String systemPrompt;

    private Double temperature;

    private List<ChatMessageItem> messages;
}
