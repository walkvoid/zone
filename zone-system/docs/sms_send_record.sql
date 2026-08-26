-- 短信发送记录（zone-system）
-- 可复用于登录验证码及其他业务场景（biz_type 区分）

CREATE TABLE IF NOT EXISTS `sms_send_record` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `biz_type`         VARCHAR(32)  NOT NULL                COMMENT '业务类型：LOGIN 等',
    `target`           VARCHAR(32)  NOT NULL                COMMENT '接收方（手机号）',
    `channel`          VARCHAR(16)  NOT NULL DEFAULT 'SMS'  COMMENT '通道：SMS',
    `code`             VARCHAR(16)  NOT NULL                COMMENT '验证码',
    `status`           VARCHAR(16)  NOT NULL                COMMENT '状态：SENT/FAILED/USED/EXPIRED',
    `fail_reason`      VARCHAR(256)                          COMMENT '失败原因',
    `client_ip`        VARCHAR(64)                           COMMENT '请求 IP',
    `provider_msg_id`  VARCHAR(128)                          COMMENT '通道回执 ID',
    `expire_time`      DATETIME     NOT NULL                COMMENT '验证码过期时间',
    `used_time`        DATETIME                              COMMENT '使用时间',
    `create_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_target_biz` (`target`, `biz_type`, `create_time`),
    KEY `idx_status_expire` (`status`, `expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信发送记录';
