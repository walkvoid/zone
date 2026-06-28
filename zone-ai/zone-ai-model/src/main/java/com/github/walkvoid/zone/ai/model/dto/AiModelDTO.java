package com.github.walkvoid.zone.ai.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI模型 DTO
 *
 * @author walkvoid
 */
@Data
public class AiModelDTO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    private String modelCode;
    private String modelName;
    private String provider;
    private String baseUrl;
    private String apiKey;
    private Long callCount;
    private Integer isEnabled;
    private Integer priority;
    private String description;
    private String configJson;
    private Long createId;
    private LocalDateTime createTime;
    private Long updateId;
    private LocalDateTime updateTime;
}
