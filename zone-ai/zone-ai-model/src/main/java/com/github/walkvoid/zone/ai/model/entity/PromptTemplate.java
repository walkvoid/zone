package com.github.walkvoid.zone.ai.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Prompt模板实体
 *
 * @author walkvoid
 */
@Data
@TableName("prompt_template")
public class PromptTemplate implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    /** 模板编码 */
    private String templateCode;

    /** 模板名称 */
    private String templateName;

    /** 模板内容 */
    private String templateContent;

    /** 变量列表 */
    private String variables;

    /** 分类 */
    private String category;

    /** 描述 */
    private String description;

    /** 状态：1=启用 0=禁用 */
    private Integer status;

    private Long createId;
    private LocalDateTime createTime;
    private Long updateId;
    private LocalDateTime updateTime;
}
