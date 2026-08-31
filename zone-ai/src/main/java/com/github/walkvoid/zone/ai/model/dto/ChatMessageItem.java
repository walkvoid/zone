package com.github.walkvoid.zone.ai.model.dto;

import lombok.Data;

/**
 * 对话消息项（OpenAI 兼容 role/content）
 */
@Data
public class ChatMessageItem {

    /** system / user / assistant */
    private String role;

    private String content;
}
