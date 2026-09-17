package com.github.walkvoid.zone.ai.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 上下文 DTO
 */
@Data
public class AiContextDTO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /** 上下文名称 */
    private String name;

    /** 上下文路径 */
    private String contextPath;

    /** 注入对话的系统提示摘要 */
    private String systemHint;

    private String description;

    /** 1=启用，0=禁用 */
    private Integer enabled;

    private Long createId;
    private LocalDateTime createTime;
    private Long updateId;
    private LocalDateTime updateTime;
}
