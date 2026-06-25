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

    /** 模板内容，含 {{变量}} 占位符 */
    private String templateContent;

    /** 变量列表 JSON */
    private String variables;

    /** 分类 */
    private String category;

    /** 描述 */
    private String description;

    /** 状态：1-启用 0-禁用 */
    private Integer status;

    /** 创建者 */
    private Long createId;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新者 */
    private Long updateId;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
