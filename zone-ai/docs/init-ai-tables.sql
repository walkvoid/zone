-- ============================================
-- zone-ai MySQL 建表脚本
-- 来源：zone-ai-model 实体 @TableName
-- 字符集：utf8mb4
-- 说明：
--   1) 当前代码实际用到的表共 4 张（见下方）
--   2) Qdrant 向量库、企微长连接会话目前不落 MySQL
--   3) 可重复执行（CREATE DATABASE / CREATE TABLE IF NOT EXISTS）
-- ============================================

CREATE DATABASE IF NOT EXISTS `zone_ai`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `zone_ai`;

-- ============================================
-- 1. AI 模型配置 (ai_model)
-- 实体：com.github.walkvoid.zone.ai.model.entity.AiModel
-- 编码唯一；is_enabled 对应 BooleanEnum：1=是，0=否
-- ============================================
CREATE TABLE IF NOT EXISTS `ai_model` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `model_code`    VARCHAR(64)   NOT NULL                COMMENT '模型编码',
    `model_name`    VARCHAR(128)  NOT NULL                COMMENT '模型名称',
    `provider`      VARCHAR(64)   DEFAULT NULL            COMMENT '供应商，如 openai / deepseek',
    `base_url`      VARCHAR(512)  DEFAULT NULL            COMMENT 'API 地址',
    `api_key`       VARCHAR(256)  DEFAULT NULL            COMMENT 'API 密钥',
    `call_count`    BIGINT        NOT NULL DEFAULT 0      COMMENT '累计调用次数',
    `is_enabled`    TINYINT       NOT NULL DEFAULT 1      COMMENT '是否启用：1-是，0-否',
    `priority`      INT           NOT NULL DEFAULT 0      COMMENT '优先级，越大越优先',
    `description`   VARCHAR(512)  DEFAULT NULL            COMMENT '描述',
    `config_json`   TEXT          DEFAULT NULL            COMMENT '扩展配置 JSON',
    `create_id`     BIGINT        DEFAULT NULL            COMMENT '创建人ID',
    `create_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`     BIGINT        DEFAULT NULL            COMMENT '更新人ID',
    `update_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_model_code` (`model_code`),
    KEY `idx_is_enabled_priority` (`is_enabled`, `priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型配置表';

-- ============================================
-- 2. Prompt 模板 (prompt_template)
-- 实体：com.github.walkvoid.zone.ai.model.entity.PromptTemplate
-- 模板内容支持 {var} 占位；status：1=启用，0=禁用
-- ============================================
CREATE TABLE IF NOT EXISTS `prompt_template` (
    `id`                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `template_code`     VARCHAR(64)   NOT NULL                COMMENT '模板编码',
    `template_name`     VARCHAR(128)  NOT NULL                COMMENT '模板名称',
    `template_content`  MEDIUMTEXT    NOT NULL                COMMENT '模板内容',
    `variables`         VARCHAR(1024) DEFAULT NULL            COMMENT '变量列表（逗号分隔或 JSON）',
    `category`          VARCHAR(64)   DEFAULT NULL            COMMENT '分类',
    `description`       VARCHAR(512)  DEFAULT NULL            COMMENT '描述',
    `status`            TINYINT       NOT NULL DEFAULT 1      COMMENT '状态：1=启用，0=禁用',
    `create_id`         BIGINT        DEFAULT NULL            COMMENT '创建人ID',
    `create_time`       DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`         BIGINT        DEFAULT NULL            COMMENT '更新人ID',
    `update_time`       DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_template_code` (`template_code`),
    KEY `idx_category_status` (`category`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Prompt模板表';

-- ============================================
-- 3. Prompt 模板运行记录 (prompt_template_run_record)
-- 实体：com.github.walkvoid.zone.ai.model.entity.PromptTemplateRunRecord
-- status：0=失败，1=成功，2=执行中
-- ============================================
CREATE TABLE IF NOT EXISTS `prompt_template_run_record` (
    `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `template_id`      BIGINT        NOT NULL                COMMENT '关联的模板ID',
    `input_params`     TEXT          DEFAULT NULL            COMMENT '本次运行入参 JSON',
    `rendered_prompt`  MEDIUMTEXT    DEFAULT NULL            COMMENT '变量替换后的最终提示词',
    `run_result`       MEDIUMTEXT    DEFAULT NULL            COMMENT '运行结果',
    `status`           TINYINT       NOT NULL DEFAULT 2      COMMENT '运行状态：0=失败，1=成功，2=执行中',
    `error_message`    VARCHAR(1024) DEFAULT NULL            COMMENT '错误信息',
    `model_name`       VARCHAR(128)  DEFAULT NULL            COMMENT '调用模型名称',
    `duration_ms`      BIGINT        DEFAULT NULL            COMMENT '执行耗时（毫秒）',
    `run_start_time`   DATETIME      DEFAULT NULL            COMMENT '开始执行时间',
    `run_end_time`     DATETIME      DEFAULT NULL            COMMENT '结束执行时间',
    `create_id`        BIGINT        DEFAULT NULL            COMMENT '触发人ID',
    `create_time`      DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_template_id` (`template_id`),
    KEY `idx_status_create_time` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Prompt模板运行记录表';

-- ============================================
-- 4. MCP Server 配置 (mcp_server_config)
-- 实体：com.github.walkvoid.zone.ai.model.entity.McpServerConfig
-- transport_type：stdio / sse / streamable-http
-- status：0=禁用，1=启用
-- running_status：0=已停止，1=运行中，2=异常
-- ============================================
CREATE TABLE IF NOT EXISTS `mcp_server_config` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `server_code`     VARCHAR(64)   NOT NULL                COMMENT '服务编码',
    `server_name`     VARCHAR(128)  NOT NULL                COMMENT '服务名称',
    `transport_type`  VARCHAR(32)   NOT NULL                COMMENT '传输类型：stdio / sse / streamable-http',
    `command`         VARCHAR(512)  DEFAULT NULL            COMMENT '启动命令（stdio 模式）',
    `args`            TEXT          DEFAULT NULL            COMMENT '命令参数 JSON 数组',
    `url`             VARCHAR(512)  DEFAULT NULL            COMMENT '服务 URL（sse / streamable-http）',
    `env_vars`        TEXT          DEFAULT NULL            COMMENT '环境变量 JSON',
    `headers`         TEXT          DEFAULT NULL            COMMENT '自定义请求头 JSON',
    `timeout_ms`      BIGINT        DEFAULT NULL            COMMENT '超时时间（毫秒）',
    `status`          TINYINT       NOT NULL DEFAULT 1      COMMENT '启用状态：0=禁用，1=启用',
    `running_status`  TINYINT       NOT NULL DEFAULT 0      COMMENT '运行状态：0=已停止，1=运行中，2=异常',
    `description`     VARCHAR(512)  DEFAULT NULL            COMMENT '描述',
    `create_id`       BIGINT        DEFAULT NULL            COMMENT '创建人ID',
    `create_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`       BIGINT        DEFAULT NULL            COMMENT '更新人ID',
    `update_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_server_code` (`server_code`),
    KEY `idx_status_running` (`status`, `running_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MCP Server配置表';
