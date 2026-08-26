package com.github.walkvoid.zone.system.db.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.walkvoid.zone.system.model.enums.SmsBizTypeEnum;
import com.github.walkvoid.zone.system.model.enums.SmsSendStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 短信发送记录
 */
@Data
@TableName("sms_send_record")
public class SmsSendRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private SmsBizTypeEnum bizType;

    /** 接收方（手机号） */
    private String target;

    /** 通道，默认 SMS */
    private String channel;

    private String code;

    private SmsSendStatusEnum status;

    private String failReason;

    private String clientIp;

    private String providerMsgId;

    private LocalDateTime expireTime;

    private LocalDateTime usedTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
