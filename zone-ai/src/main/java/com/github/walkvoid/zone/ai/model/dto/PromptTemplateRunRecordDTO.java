package com.github.walkvoid.zone.ai.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * PromptTemplate运行记录 DTO
 *
 * @author walkvoid
 */
@Data
public class PromptTemplateRunRecordDTO implements Serializable {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long templateId;

    private String inputParams;

    private String renderedPrompt;

    private String runResult;

    /** 关联文档 id 列表 JSON */
    private String fileIds;

    private Integer status;

    private String errorMessage;

    private String modelName;

    private Long durationMs;

    private LocalDateTime runStartTime;

    private LocalDateTime runEndTime;

    private Long createId;

    private LocalDateTime createTime;
}
