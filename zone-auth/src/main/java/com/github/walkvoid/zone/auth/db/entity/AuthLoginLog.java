package com.github.walkvoid.zone.auth.db.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.walkvoid.zone.auth.model.enums.LoginTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录审计日志
 */
@Data
@TableName("auth_login_log")
public class AuthLoginLog implements Serializable {

    @TableId
    private Long id;

    private Long userId;

    private String username;

    private LoginTypeEnum loginType;

    /** 0-失败，1-成功 */
    private Integer success;

    private String failReason;

    private String clientIp;

    private String userAgent;

    private LocalDateTime createTime;
}
