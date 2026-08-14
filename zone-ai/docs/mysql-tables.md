# zone-ai MySQL 表说明

可执行脚本：[`init-ai-tables.sql`](./init-ai-tables.sql)

按当前 `zone-ai-model` 实体整理，库名 **`zone-ai`**，**代码里真正用到的 MySQL 表共 4 张**。向量检索走 Qdrant，不在本脚本内。

脚本会先 `CREATE DATABASE IF NOT EXISTS zone-ai` 再建模。MyBatis 使用 `spring.datasource.*`（库名 `zone-ai`），与 SqlQueryTool 同一台 MySQL、同一账号。

## 表清单

| 表名 | 实体 | 用途 |
|---|---|---|
| `ai_model` | `AiModel` | 大模型配置（编码、供应商、baseUrl、apiKey、启用、优先级） |
| `prompt_template` | `PromptTemplate` | Prompt 模板（内容、变量、分类、启用状态） |
| `prompt_template_run_record` | `PromptTemplateRunRecord` | 模板运行记录（入参、渲染结果、耗时、成功/失败） |
| `mcp_server_config` | `McpServerConfig` | 外部 MCP 连接配置（stdio / sse / streamable-http） |

## 使用方式

无需先选库，直接执行：

```bash
mysql -u<user> -p < zone-ai/docs/init-ai-tables.sql
```

或在客户端打开 `init-ai-tables.sql` 整文件执行。可重复跑。

## 字段约定

- 主键 `BIGINT AUTO_INCREMENT`，与仓库内其它模块建表风格一致。
- `is_enabled` / 各类 `status` 用 `TINYINT`：`BooleanEnum` 为 **1=是，0=否**。
- `prompt_template_run_record.status`：`0=失败，1=成功，2=执行中`。
- `mcp_server_config.status`：`0=禁用，1=启用`；`running_status`：`0=已停止，1=运行中，2=异常`。
- 长文本（模板内容、运行结果、JSON 配置）用 `TEXT` / `MEDIUMTEXT`。
- `model_code`、`template_code`、`server_code` 建了唯一索引，对应 DAO 的 `checkCodeExists`。

## 不在 MySQL 里的部分

| 数据 | 存放 |
|---|---|
| 供应链金融知识向量 | Qdrant 集合 `supply_finance_rag` |
| 企微长连接会话 | 当前仅内存，无表 |
| BeeCloud 日志 | 外部日志平台，不落本地库 |

## 规划中（实施计划后续才建，本脚本未包含）

见 [`ai-programmer-agent-plan.md`](./ai-programmer-agent-plan.md)：

- Agent 对话审计日志（question / tools_called / answer）
- 代码改动审计单（PR 分支、WAITING_AUDIT 等状态）

等对应实体落地后再补 DDL。
