-- ============================================
-- Zone Auth 阶段 3：Refresh 会话 + 登录审计
-- ============================================

-- Refresh Token 会话（支持轮换、踢人、登出撤销）
CREATE TABLE IF NOT EXISTS `auth_refresh_token` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT       NOT NULL                COMMENT '用户ID',
    `token_hash`  VARCHAR(64)  NOT NULL                COMMENT 'Refresh Token SHA-256',
    `status`      VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/REVOKED/ROTATED',
    `expires_at`  DATETIME     NOT NULL                COMMENT '过期时间',
    `client_ip`   VARCHAR(64)  DEFAULT NULL            COMMENT '客户端 IP',
    `user_agent`  VARCHAR(512) DEFAULT NULL            COMMENT 'User-Agent',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_token_hash` (`token_hash`),
    KEY `idx_user_status` (`user_id`, `status`),
    KEY `idx_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Refresh Token 会话';

-- 登录审计
CREATE TABLE IF NOT EXISTS `auth_login_log` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT       DEFAULT NULL            COMMENT '用户ID',
    `username`    VARCHAR(64)  DEFAULT NULL            COMMENT '登录名',
    `login_type`  VARCHAR(32)  NOT NULL DEFAULT 'PASSWORD' COMMENT 'PASSWORD/REGISTER',
    `success`     TINYINT      NOT NULL                COMMENT '0-失败，1-成功',
    `fail_reason` VARCHAR(256) DEFAULT NULL            COMMENT '失败原因',
    `client_ip`   VARCHAR(64)  DEFAULT NULL            COMMENT '客户端 IP',
    `user_agent`  VARCHAR(512) DEFAULT NULL            COMMENT 'User-Agent',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_username` (`username`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录审计日志';
