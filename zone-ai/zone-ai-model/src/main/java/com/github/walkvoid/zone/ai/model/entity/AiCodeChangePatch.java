package com.github.walkvoid.zone.ai.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 代码改动中的单个文件 patch。
 */
@Data
@TableName("ai_code_change_patch")
public class AiCodeChangePatch implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private Long changeId;
    private String sourcePath;
    private String patchFile;
    private String toolName;
    private Integer newFile;
    private Integer addedLines;
    private Integer removedLines;
    private String unifiedDiff;
    private String baseContent;
    private String newContent;

    /**
     * 0=待应用，1=已应用，2=冲突，3=失败
     */
    private Integer status;
    private String errorMessage;
    private LocalDateTime applyTime;
    private LocalDateTime createTime;
}
