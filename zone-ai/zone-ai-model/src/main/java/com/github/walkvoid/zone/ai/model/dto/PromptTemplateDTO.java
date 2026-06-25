package com.github.walkvoid.zone.ai.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Prompt模板 DTO
 *
 * @author walkvoid
 */
@Data
public class PromptTemplateDTO {

    private Long id;
    private String templateCode;
    private String templateName;
    private String templateContent;
    private String variables;
    private String category;
    private String description;
    private Integer status;
    private Long createId;
    private LocalDateTime createTime;
    private Long updateId;
    private LocalDateTime updateTime;
}
