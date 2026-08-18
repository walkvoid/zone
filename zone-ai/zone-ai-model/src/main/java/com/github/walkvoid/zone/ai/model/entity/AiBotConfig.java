package com.github.walkvoid.zone.ai.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.walkvoid.wvframework.models.BooleanEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 智能机器人配置。一 bot 一条企微长连接，各自 prompt 与工具集。
 * 仅 zone-ai-business 使用，不放在 zone-ai-model，避免只编 business 时拿到旧 model jar。
 */
@Data
@TableName("ai_bot_config")
public class AiBotConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    /** 内部编码，如 supply-chain */
    private String botCode;

    /** 企微 aibotid */
    private String botId;

    /** 展示名 */
    private String botName;

    /** 长连接 Secret */
    private String secret;

    /** 通道：WEIXIN / FEISHU */
    private String channelType;

    /** 系统提示词 */
    private String systemPrompt;

    /**
     * 工具编码，逗号分隔：log,sql,repo_read,repo_change
     */
    private String toolCodes;

    /** 进入会话欢迎语 */
    private String welcomeText;

    /** 是否启用 */
    private BooleanEnum isEnabled;

    private String description;

    private Long createId;
    private LocalDateTime createTime;
    private Long updateId;
    private LocalDateTime updateTime;
}
