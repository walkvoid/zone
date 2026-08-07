package com.github.walkvoid.zone.ai.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * PromptTemplate运行记录实体
 *
 * @author walkvoid
 */
@Data
@TableName("prompt_template_run_record")
public class PromptTemplateRunRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    /** 关联的模板ID */
    private Long templateId;

    /** 本次运行的入参（JSON格式） */
    private String inputParams;

    /** 变量替换后的最终提示词 */
    private String renderedPrompt;

    /** 运行结果 */
    private String runResult;

    /** 运行状态：0=失败，1=成功，2=执行中 */
    private Integer status;

    /** 错误信息 */
    private String errorMessage;

    /** 调用模型名称 */
    private String modelName;

    /** 执行耗时（毫秒） */
    private Long durationMs;

    /** 开始执行时间 */
    private LocalDateTime runStartTime;

    /** 结束执行时间 */
    private LocalDateTime runEndTime;

    /** 触发人ID */
    private Long createId;

    /** 创建时间 */
    private LocalDateTime createTime;
}
