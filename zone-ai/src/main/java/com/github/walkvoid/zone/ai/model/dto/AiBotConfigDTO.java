package com.github.walkvoid.zone.ai.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 智能机器人配置 DTO。
 * {@code secret} 查询时为掩码；更新时留空表示不改密钥。
 */
@Data
public class AiBotConfigDTO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    private String botCode;
    private String botId;
    private String botName;
    private String secret;
    private Boolean hasSecret;
    private String channelType;
    private String systemPrompt;
    private String toolCodes;
    private String welcomeText;
    /** 1=启用，0=禁用 */
    private Integer isEnabled;
    private String description;
    private Long createId;
    private LocalDateTime createTime;
    private Long updateId;
    private LocalDateTime updateTime;
}
