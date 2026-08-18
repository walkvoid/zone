package com.github.walkvoid.zone.ai.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 一次对话轮次产生的代码改动（一个小功能点，可含多个 patch）。
 */
@Data
@TableName("ai_code_change")
public class AiCodeChange implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    /** 群会话 key，如 weixin:{botId}:{chatId} */
    private String conversationId;

    /** 本轮 askModel 的唯一 ID，同一轮多次 apply 共用 */
    private String turnNo;

    private String messageId;
    private String botId;
    private String botCode;
    private String chatId;
    private String userId;
    private String channelType;

    /** 用户原话摘要，作为功能点标题 */
    private String title;
    private String requestText;

    /** DIFF_FILE / DIRECT */
    private String writeMode;

    /**
     * 0=待应用（DIFF_FILE），1=已写入源文件，2=部分成功，3=失败
     */
    private Integer status;

    private Integer patchCount;
    private String applyMessage;
    private LocalDateTime applyTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
