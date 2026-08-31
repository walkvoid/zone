package com.github.walkvoid.zone.ai.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 上下文包明细 DTO
 */
@Data
public class AiContextPackDetailDTO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long packId;

    /** FILE / CODE / OTHER */
    private String detailType;

    private String title;
    private String content;
    private String refId;
    private String configJson;
    private Integer sortOrder;
    private Long createId;
    private LocalDateTime createTime;
    private Long updateId;
    private LocalDateTime updateTime;
}
