package com.github.walkvoid.zone.ai.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 上下文包 DTO（含明细列表）
 */
@Data
public class AiContextPackDTO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    private String packCode;
    private String packName;
    private String systemHint;
    private String description;
    private Integer isEnabled;
    private Long createId;
    private LocalDateTime createTime;
    private Long updateId;
    private LocalDateTime updateTime;

    /** 明细条数（列表页用） */
    private Integer detailCount;

    private List<AiContextPackDetailDTO> details;
}
