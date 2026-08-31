-- ============================================
-- 上下文包：ai_context_pack / ai_context_pack_detail
-- 一对多；detail.detail_type：FILE / CODE / OTHER
-- ============================================

CREATE TABLE IF NOT EXISTS `ai_context_pack` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `pack_code`     VARCHAR(64)   NOT NULL                COMMENT '上下文包编码',
    `pack_name`     VARCHAR(128)  NOT NULL                COMMENT '上下文包名称',
    `system_hint`   TEXT          DEFAULT NULL            COMMENT '注入对话的系统提示摘要',
    `description`   VARCHAR(512)  DEFAULT NULL            COMMENT '描述',
    `is_enabled`    TINYINT       NOT NULL DEFAULT 1      COMMENT '是否启用：1-是，0-否',
    `create_id`     BIGINT        DEFAULT NULL            COMMENT '创建人ID',
    `create_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`     BIGINT        DEFAULT NULL            COMMENT '更新人ID',
    `update_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pack_code` (`pack_code`),
    KEY `idx_is_enabled` (`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI上下文包';

CREATE TABLE IF NOT EXISTS `ai_context_pack_detail` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `pack_id`       BIGINT        NOT NULL                COMMENT '所属上下文包ID',
    `detail_type`   VARCHAR(32)   NOT NULL                COMMENT '类型：FILE / CODE / OTHER',
    `title`         VARCHAR(256)  DEFAULT NULL            COMMENT '标题',
    `content`       MEDIUMTEXT    DEFAULT NULL            COMMENT '正文：文件说明/源码范围/其它文本',
    `ref_id`        VARCHAR(128)  DEFAULT NULL            COMMENT '外部引用，如 file_info.id',
    `config_json`   TEXT          DEFAULT NULL            COMMENT '扩展配置 JSON（分支、路径、表白名单等）',
    `sort_order`    INT           NOT NULL DEFAULT 0      COMMENT '排序，越小越靠前',
    `create_id`     BIGINT        DEFAULT NULL            COMMENT '创建人ID',
    `create_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`     BIGINT        DEFAULT NULL            COMMENT '更新人ID',
    `update_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_pack_id` (`pack_id`),
    KEY `idx_pack_type` (`pack_id`, `detail_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI上下文包明细';
