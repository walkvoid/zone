-- ============================================
-- Zone Auth 认证域建表（与 zone-user 同库）
-- ============================================

-- 登录身份
CREATE TABLE IF NOT EXISTS `user_identity` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`        BIGINT       NOT NULL                COMMENT '用户ID',
    `identity_type`  VARCHAR(32)  NOT NULL                COMMENT '身份类型：USERNAME/PHONE/EMAIL',
    `identifier`     VARCHAR(128) NOT NULL                COMMENT '身份标识',
    `verified`       TINYINT      DEFAULT 0               COMMENT '是否已验证：0-否，1-是',
    `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_identity` (`identity_type`, `identifier`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户登录身份';

-- 凭证（密码等）
CREATE TABLE IF NOT EXISTS `user_credential` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`          BIGINT       NOT NULL                COMMENT '用户ID',
    `credential_type`  VARCHAR(32)  NOT NULL                COMMENT '凭证类型：PASSWORD',
    `secret_hash`      VARCHAR(256) NOT NULL                COMMENT '密文（BCrypt）',
    `create_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_credential` (`user_id`, `credential_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户凭证';

-- 阶段 3 表见 docs/init-auth-phase3.sql（auth_refresh_token、auth_login_log）
