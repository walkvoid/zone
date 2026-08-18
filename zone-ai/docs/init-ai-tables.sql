-- ============================================
-- zone-ai MySQL 建表脚本
-- 来源：zone-ai-model 实体 @TableName
-- 字符集：utf8mb4
-- 说明：
--   1) 当前代码实际用到的表共 9 张（见下方）
--   2) Qdrant 向量库、企微长连接会话目前不落 MySQL；机器人凭证/prompt/工具集落 ai_bot_config
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

-- ============================================
-- 5. 智能机器人配置 (ai_bot_config)
-- 实体：com.github.walkvoid.zone.ai.business.db.entity.AiBotConfig
-- 一 bot 一条企微 WebSocket；system_prompt / tool_codes 按 bot 隔离
-- tool_codes 逗号分隔：log,sql,repo_read,repo_change
-- is_enabled 对应 BooleanEnum：1=是，0=否
-- secret 请填长连接专用密钥，不要把生产密钥提交进 Git
-- ============================================
CREATE TABLE IF NOT EXISTS `ai_bot_config` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `bot_code`        VARCHAR(64)   NOT NULL                COMMENT '内部编码，如 supply-chain',
    `bot_id`          VARCHAR(128)  NOT NULL                COMMENT '企微 aibotid',
    `bot_name`        VARCHAR(128)  NOT NULL                COMMENT '展示名称',
    `secret`          VARCHAR(256)  DEFAULT NULL            COMMENT '长连接 Secret',
    `channel_type`    VARCHAR(32)   NOT NULL DEFAULT 'WEIXIN' COMMENT '通道：WEIXIN / FEISHU',
    `system_prompt`   MEDIUMTEXT    NOT NULL                COMMENT '系统提示词',
    `tool_codes`      VARCHAR(512)  NOT NULL DEFAULT 'log,sql,repo_read' COMMENT '工具编码，逗号分隔',
    `welcome_text`    VARCHAR(512)  DEFAULT NULL            COMMENT '进入会话欢迎语',
    `is_enabled`      TINYINT       NOT NULL DEFAULT 1      COMMENT '是否启用：1-是，0-否',
    `description`     VARCHAR(512)  DEFAULT NULL            COMMENT '描述',
    `create_id`       BIGINT        DEFAULT NULL            COMMENT '创建人ID',
    `create_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`       BIGINT        DEFAULT NULL            COMMENT '更新人ID',
    `update_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_bot_code` (`bot_code`),
    UNIQUE KEY `uk_bot_id` (`bot_id`),
    KEY `idx_channel_enabled` (`channel_type`, `is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能机器人配置表';

-- 示例：供应链排障助手（secret 请改成本地企微长连接密钥后再启用）
INSERT INTO `ai_bot_config` (
    `bot_code`, `bot_id`, `bot_name`, `secret`, `channel_type`,
    `system_prompt`, `tool_codes`, `welcome_text`, `is_enabled`, `description`
) SELECT
    'supply-chain',
    'REPLACE_WITH_AIBOT_ID',
    '供应链排障助手',
    '',
    'WEIXIN',
    '你是供应链金融排障助手，在企业微信群里回答。\n规则：\n1. 用户给 traceId、报错、某环境刚失败时，调用 beecloudSearchLogs；env 常见为 dev 或 qa。\n2. 查用户、合同、融资单、流水、资方等业务数据时，先 listNamedQueries，再 runNamedQuery。禁止手写 SQL。\n3. 问「代码在哪」「哪个类/方法」「这段逻辑怎么实现」时，先 listRepos 看沙箱，再 searchCode，需要细节时 readSourceFile。\n4. 用户明确要求改代码时：先 readSourceFile，再 describeWritePolicy 看 write-mode，然后直接 applyPatch 或 applyReplace。\n   write-mode=DIFF_FILE 时 apply 只在源文件同级生成 .patch，不改源文件；DIRECT 才覆盖沙箱源文件。\n5. 用户发截图或图片时，先识别图中的文字、报错、traceId、单号，再按上面规则调用工具。\n6. 不知道就说不知道，禁止编造状态码、金额、接口路径、类名。\n7. 回复要短，适合群聊。工具原始 JSON 只提炼结论，不要整段贴回群。\n8. 同一群内的连续提问属于同一段对话，可沿用上文中的单号、traceId、结论。\n9. 记忆里只有用户短文本和你的短回复，没有工具原始 JSON；需要最新数据时再调工具。',
    'log,sql,repo_read,repo_change',
    '你好，我是供应链排障助手。',
    0,
    '示例数据：填好 bot_id/secret 后把 is_enabled 改为 1'
WHERE NOT EXISTS (
    SELECT 1 FROM `ai_bot_config` WHERE `bot_code` = 'supply-chain'
);

-- ============================================
-- 6. AI 代码改动（ai_code_change）
-- 一条记录 = 一次对话轮次里的一个小功能点，可含多个 patch
-- conversation_id 与群记忆 key 相同：weixin:{botId}:{chatId}
-- turn_no 为本轮 askModel UUID，同一轮多次 applyPatch 归到同一条
-- write_mode：DIFF_FILE / DIRECT
-- status：0=待应用，1=已写入源文件，2=部分成功，3=失败
-- ============================================
CREATE TABLE IF NOT EXISTS `ai_code_change` (
    `id`                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `conversation_id`   VARCHAR(256)  NOT NULL                COMMENT '对话会话 key',
    `turn_no`           VARCHAR(64)   NOT NULL                COMMENT '本轮问答号',
    `message_id`        VARCHAR(128)  DEFAULT NULL            COMMENT '通道消息 ID',
    `bot_id`            VARCHAR(128)  DEFAULT NULL            COMMENT '企微 aibotid',
    `bot_code`          VARCHAR(64)   DEFAULT NULL            COMMENT '内部机器人编码',
    `chat_id`           VARCHAR(128)  DEFAULT NULL            COMMENT '群 ID',
    `user_id`           VARCHAR(128)  DEFAULT NULL            COMMENT '提问人',
    `channel_type`      VARCHAR(32)   DEFAULT NULL            COMMENT 'WEIXIN / FEISHU',
    `title`             VARCHAR(160)  NOT NULL                COMMENT '功能点标题（用户原话摘要）',
    `request_text`      VARCHAR(1024) DEFAULT NULL            COMMENT '用户原话',
    `write_mode`        VARCHAR(32)   NOT NULL                COMMENT 'DIFF_FILE / DIRECT',
    `status`            TINYINT       NOT NULL DEFAULT 0      COMMENT '0待应用 1已应用 2部分成功 3失败',
    `patch_count`       INT           NOT NULL DEFAULT 0      COMMENT 'patch 文件数',
    `apply_message`     VARCHAR(512)  DEFAULT NULL            COMMENT '最近一次应用结果',
    `apply_time`        DATETIME      DEFAULT NULL            COMMENT '应用到源文件的时间',
    `create_time`       DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_turn_no` (`turn_no`),
    KEY `idx_conversation_time` (`conversation_id`, `create_time`),
    KEY `idx_bot_status` (`bot_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI代码改动（功能点）';

-- ============================================
-- 7. AI 代码改动文件（ai_code_change_patch）
-- 一个功能点下的单个源文件 unified diff；new_content 用于页面 Apply
-- status：0=待应用，1=已应用，2=冲突，3=失败
-- ============================================
CREATE TABLE IF NOT EXISTS `ai_code_change_patch` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `change_id`       BIGINT        NOT NULL                COMMENT 'ai_code_change.id',
    `source_path`     VARCHAR(512)  NOT NULL                COMMENT '相对沙箱的源文件路径',
    `patch_file`      VARCHAR(512)  DEFAULT NULL            COMMENT 'DIFF_FILE 写出的 .patch 相对路径',
    `tool_name`       VARCHAR(32)   DEFAULT NULL            COMMENT 'applyPatch / applyReplace',
    `new_file`        TINYINT       NOT NULL DEFAULT 0      COMMENT '1=新建文件',
    `added_lines`     INT           NOT NULL DEFAULT 0      COMMENT '新增行',
    `removed_lines`   INT           NOT NULL DEFAULT 0      COMMENT '删除行',
    `unified_diff`    MEDIUMTEXT    DEFAULT NULL            COMMENT 'unified diff 全文',
    `base_content`    MEDIUMTEXT    DEFAULT NULL            COMMENT '生成时的源文件内容，用于冲突检测',
    `new_content`     MEDIUMTEXT    DEFAULT NULL            COMMENT '目标全文，Apply 时写入',
    `status`          TINYINT       NOT NULL DEFAULT 0      COMMENT '0待应用 1已应用 2冲突 3失败',
    `error_message`   VARCHAR(512)  DEFAULT NULL            COMMENT '应用失败原因',
    `apply_time`      DATETIME      DEFAULT NULL            COMMENT '应用时间',
    `create_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_change_id` (`change_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI代码改动文件';

-- ============================================
-- 8. Agent 一轮问答 (ai_agent_turn)
-- 与群记忆 conversation_id、改代码 turn_no 对齐
-- 写库走进程内异步队列，不挡对话
-- status：0=运行中，1=成功，2=失败，3=超时
-- ============================================
CREATE TABLE IF NOT EXISTS `ai_agent_turn` (
    `id`                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `conversation_id`   VARCHAR(256)  NOT NULL                COMMENT '对话会话 key',
    `session_id`        VARCHAR(320)  NOT NULL                COMMENT '上下文会话段 ID（超时/重置会切换）',
    `turn_no`           VARCHAR(64)   NOT NULL                COMMENT '本轮问答号',
    `message_id`        VARCHAR(128)  DEFAULT NULL            COMMENT '通道消息 ID',
    `bot_id`            VARCHAR(128)  DEFAULT NULL            COMMENT '企微 aibotid',
    `bot_code`          VARCHAR(64)   DEFAULT NULL            COMMENT '内部机器人编码',
    `chat_id`           VARCHAR(128)  DEFAULT NULL            COMMENT '群 ID',
    `user_id`           VARCHAR(128)  DEFAULT NULL            COMMENT '提问人',
    `channel_type`      VARCHAR(32)   DEFAULT NULL            COMMENT 'WEIXIN / FEISHU',
    `user_text`         VARCHAR(1024) DEFAULT NULL            COMMENT '用户原话（截断）',
    `has_image`         TINYINT       NOT NULL DEFAULT 0      COMMENT '是否带图',
    `final_answer`      MEDIUMTEXT    DEFAULT NULL            COMMENT '最终回复（截断）',
    `status`            TINYINT       NOT NULL DEFAULT 0      COMMENT '0运行中 1成功 2失败 3超时',
    `error_message`     VARCHAR(512)  DEFAULT NULL            COMMENT '失败原因',
    `tool_call_count`   INT           NOT NULL DEFAULT 0      COMMENT '工具调用次数',
    `duration_ms`       BIGINT        DEFAULT NULL            COMMENT '本轮耗时',
    `create_time`       DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    `finish_time`       DATETIME      DEFAULT NULL            COMMENT '结束时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_turn_no` (`turn_no`),
    KEY `idx_session_time` (`session_id`, `create_time`),
    KEY `idx_conversation_time` (`conversation_id`, `create_time`),
    KEY `idx_bot_time` (`bot_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent一轮问答';

-- ============================================
-- 9. Agent 工具步骤 (ai_agent_step)
-- 无外键：异步时步骤可能先于头表到达，按 turn_no 关联
-- request_json / response_json 已截断脱敏
-- ============================================
CREATE TABLE IF NOT EXISTS `ai_agent_step` (
    `id`                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `turn_no`           VARCHAR(64)   NOT NULL                COMMENT '本轮问答号',
    `seq`               INT           NOT NULL DEFAULT 0      COMMENT '步骤序号',
    `step_type`         VARCHAR(32)   NOT NULL DEFAULT 'TOOL' COMMENT 'TOOL',
    `tool_code`         VARCHAR(32)   DEFAULT NULL            COMMENT 'log / sql / repo_read / repo_change',
    `tool_name`         VARCHAR(64)   DEFAULT NULL            COMMENT '方法名',
    `request_json`      TEXT          DEFAULT NULL            COMMENT '入参 JSON（截断）',
    `response_json`     MEDIUMTEXT    DEFAULT NULL            COMMENT '出参 JSON（截断）',
    `response_summary`  VARCHAR(512)  DEFAULT NULL            COMMENT '给人看的摘要',
    `success`           TINYINT       NOT NULL DEFAULT 1      COMMENT '1成功 0失败',
    `duration_ms`       BIGINT        DEFAULT NULL            COMMENT '工具耗时',
    `error_message`     VARCHAR(512)  DEFAULT NULL            COMMENT '失败原因',
    `create_time`       DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_turn_seq` (`turn_no`, `seq`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent工具步骤';
