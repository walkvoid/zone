package com.github.walkvoid.zone.auth.db.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.walkvoid.zone.auth.model.enums.SessionStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Refresh Token 会话
 */
@Data
@TableName("auth_refresh_token")
public class AuthRefreshToken implements Serializable {

    @TableId
    private Long id;

    private Long userId;

    private String tokenHash;

    private SessionStatusEnum status;

    private LocalDateTime expiresAt;

    private String clientIp;

    private String userAgent;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
