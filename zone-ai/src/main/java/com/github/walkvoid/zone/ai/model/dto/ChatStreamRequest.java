package com.github.walkvoid.zone.ai.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
     * 未传且指定了 promptTemplateId 时，使用该模板内容。
     */
    private String systemPrompt;

    /**
     * 可选。关联 prompt_template 记录，将其 templateContent 作为系统提示。
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long promptTemplateId;

    /**
     * 可选。关联 ai_context_pack，将其 systemHint 与明细拼入系统提示。
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long contextPackId;

    private Double temperature;

    private List<ChatMessageItem> messages;
}
