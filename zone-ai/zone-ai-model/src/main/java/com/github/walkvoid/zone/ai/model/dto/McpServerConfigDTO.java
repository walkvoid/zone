package com.github.walkvoid.zone.ai.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MCP Server配置 DTO
 *
 * @author walkvoid
 */
@Data
public class McpServerConfigDTO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String serverCode;

    private String serverName;

    private String transportType;

    private String command;

    private String args;

    private String url;

    private String envVars;

    private String headers;

    private Long timeoutMs;

    private Integer status;

    private Integer runningStatus;

    private String description;

    private Long createId;

    private LocalDateTime createTime;

    private Long updateId;

    private LocalDateTime updateTime;
}
