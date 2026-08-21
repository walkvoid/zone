package com.github.walkvoid.zone.ai.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiCodeChangePatchDTO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long changeId;
    private String sourcePath;
    private String patchFile;
    private String toolName;
    private Integer newFile;
    private Integer addedLines;
    private Integer removedLines;
    private String unifiedDiff;
    private Integer status;
    private String errorMessage;
    private LocalDateTime applyTime;
    private LocalDateTime createTime;
}
