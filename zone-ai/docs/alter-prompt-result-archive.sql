-- Prompt 运行结果归档表（DbInsertTool / file_upload 后处理用）
-- 已有库执行本脚本即可

CREATE TABLE IF NOT EXISTS `prompt_result_archive` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `biz_code`     VARCHAR(64)   NOT NULL DEFAULT 'prompt_run' COMMENT '业务编码',
    `title`        VARCHAR(256)  DEFAULT NULL            COMMENT '标题',
    `content`      MEDIUMTEXT    NOT NULL                COMMENT '结果正文',
    `meta_json`    TEXT          DEFAULT NULL            COMMENT '扩展元数据 JSON',
    `source_tool`  VARCHAR(64)   DEFAULT NULL            COMMENT '来源工具编码',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除: 0=未删, 1=已删',
    PRIMARY KEY (`id`),
    KEY `idx_biz_code` (`biz_code`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Prompt运行结果归档';
