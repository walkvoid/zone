package com.github.walkvoid.zone.auth.db.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.walkvoid.zone.auth.model.enums.CredentialTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户凭证（密码等）
 */
@Data
@TableName("user_credential")
public class UserCredential implements Serializable {

    @TableId
    private Long id;

    private Long userId;

    private CredentialTypeEnum credentialType;

    /** BCrypt 等加密后的密文 */
    private String secretHash;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
