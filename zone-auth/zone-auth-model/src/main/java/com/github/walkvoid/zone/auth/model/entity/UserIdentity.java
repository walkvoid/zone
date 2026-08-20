package com.github.walkvoid.zone.auth.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.walkvoid.zone.auth.model.enums.IdentityTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户登录身份（用户名/手机/邮箱）
 */
@Data
@TableName("user_identity")
public class UserIdentity implements Serializable {

    @TableId
    private Long id;

    private Long userId;

    private IdentityTypeEnum identityType;

    private String identifier;

    /** 是否已验证：0-否，1-是 */
    private Integer verified;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
