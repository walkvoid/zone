-- ============================================
-- 上下文：ai_context
-- ============================================

CREATE TABLE IF NOT EXISTS `ai_context` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`          VARCHAR(128)  NOT NULL                COMMENT '上下文名称',
    `context_path`  VARCHAR(512)  NOT NULL                COMMENT '上下文路径',
    `system_hint`   TEXT          DEFAULT NULL            COMMENT '注入对话的系统提示摘要',
    `description`   VARCHAR(512)  DEFAULT NULL            COMMENT '描述',
    `enabled`       TINYINT       NOT NULL DEFAULT 1      COMMENT '是否启用：1-是，0-否',
    `create_id`     BIGINT        DEFAULT NULL            COMMENT '创建人ID',
    `create_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`     BIGINT        DEFAULT NULL            COMMENT '更新人ID',
    `update_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    UNIQUE KEY `uk_context_path` (`context_path`),
    KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI上下文';
