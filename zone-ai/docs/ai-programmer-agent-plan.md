# Zone AI 程序员替身实施计划

> 面向供应链金融研发场景：企业微信群 `@机器人` → Agent（RAG + Tools）→ 回答 / 改代码 → **人工审计后才发布**。
>
> 文档位置：`zone-ai/docs/ai-programmer-agent-plan.md`  
> 对应代码：`zone-ai/zone-ai-business`  
> 技术栈：Spring Boot 3.3 / JDK 21 / Spring AI 1.1.8 / Qdrant / 企业微信智能机器人长连接

---

## 1. 背景与目标

供应链金融日常排障高度依赖「人肉串联」：看 BeeCloud 日志、翻 `zone-finance` 源码、查业务库、对照产品文档、再改代码发版。本项目要把这些能力接到一个 **程序员替身 Agent** 上，让群里 `@` 即可驱动大模型调用工具。

### 1.1 产品目标

| 场景 | 期望行为 |
|---|---|
| 知识问答 | 根据已入库的供应链金融文档 / 状态码 / 接口说明回答，并尽量引用出处 |
| 线上排障 | 按 traceId / 报错信息查 BeeCloud 日志，必要时再读代码、查库 |
| 代码定位 | 在白名单仓库内搜索、阅读源码，指出类 / 方法 / 行号 |
| 只读查库 | 用只读账号查业务 MySQL（订单、融资单、状态），禁止写库 |
| 改代码 | 只在沙箱产出补丁，推审计分支 / 开 PR，**禁止直接推 main 和发版** |
| 发布 | 仅人工 Review 通过后合并、发布 |

### 1.2 非目标（第一期明确不做）

- 个人微信（本通道是**企业微信智能机器人**长连接）
- AI 直接 `push` 到 `main` / `master` / `release`
- AI 执行 `--force`、改 git config、改生产配置 / 证书
- 无限制 SQL（DDL、多语句、导出文件）
- 无限制读盘（`.env`、密钥、任意绝对路径）
- 飞书（通道已留占位，不阻塞主路径）

### 1.3 成功标准（MVP）

群里 `@机器人` 能完成下面三件事，即视为 MVP 成立：

1. 问「融资成功状态是什么」→ 结合 Qdrant 知识给出 `code` / 中文含义  
2. 给一个 dev 环境 traceId → 调 BeeCloud 日志并给出失败原因摘要  
3. 问「这段校验在哪个类」→ 在白名单仓库内定位到文件和函数  

「自动改代码 + PR 审计」放在 MVP 之后，作为第二阶段。

---

## 2. 总体架构

```
企业微信群 @智能机器人
        │
        ▼
企业微信开放平台  wss://openws.work.weixin.qq.com
        │  长连接（zone-ai 主动连出，无需公网回调）
        ▼
┌─────────────────────────────────────────────────────────┐
│ zone-ai-business                                        │
│                                                         │
│  Channel（已有骨架）                                     │
│    WeiXinAiBotClient  订阅 / 心跳 / 收消息 / 流式回复     │
│    ChannelMessageHandler  ← 需从 Echo 换成 Agent         │
│           │                                             │
│           ▼                                             │
│  AgentRuntime（缺失，核心）                              │
│    ├─ 权限 / 会话记忆 / 工具轮次上限                      │
│    ├─ RAG：Qdrant 供应链金融知识                         │
│    ├─ Tools                                             │
│    │    ├─ BeeCloud 日志（已有，需加固）                  │
│    │    ├─ 读代码 / 搜仓库（缺失）                        │
│    │    ├─ 只读 MySQL（缺失）                             │
│    │    └─ 改代码 + 审计分支 / PR（缺失）                 │
│    └─ 审计单 / 对话落库（缺失）                           │
└─────────────────────────────────────────────────────────┘
        │
        ▼
人工审计（GitHub/GitLab PR 或企微卡片）→ 合并 → 发布
```

两条入口共用同一套 Tool，不要做成两套逻辑：

| 入口 | 协议 | 用途 |
|---|---|---|
| 企业微信群 | 智能机器人 WebSocket | 业务同学 / 研发在群里问 |
| Cursor 等 | MCP Server（SSE） | IDE 里调用同一批 `@Tool` |

---

## 3. 现状盘点

### 3.1 已具备

| 能力 | 说明 | 关键位置 |
|---|---|---|
| 企微长连接骨架 | 订阅 `aibot_subscribe`、心跳 ping、收 `aibot_msg_callback`、流式 `aibot_respond_msg`、进入会话欢迎语 | `channel/weixin/*` |
| 通道抽象 | `SmartLifecycle` 启停；飞书占位，便于后续扩展 | `channel/core/*`、`channel/feishu/*` |
| 大模型 | OpenAI 兼容网关 `https://atk.llschain.com`，chat=`deepseek/deepseek-v4-pro`，embedding=`text-embedding-3-small` | `application-lls.properties` |
| 日志 Tool | `beecloudSearchLogs`，POST BeeCloud search API；已挂 MCP | `tool/CodeAssistantTool.java`、`config/AIClientConfig.java` |
| MCP Server | `spring-ai-starter-mcp-server-webmvc`，SSE，`zone-ai-log` | `application.properties` |
| Qdrant | 集合 `supply_finance_rag`；测试可写入并检索融资状态枚举 | `db/vec/QdrantRagDAO.java` |
| Prompt 模板 CRUD | 模板、模型配置、运行记录（管理面，未接入 Agent） | `controller/PromptTemplate*.java` |
| ChatClient + Tool | 测试里可跑通「问日志 → 调 Tool」 | `BaseTest.testAiInvokeLogSearchTool` |

### 3.2 缺口（按优先级）

| 优先级 | 缺口 | 影响 |
|---|---|---|
| P0 | 通道 Handler 仍是 Echo，生产没有 Agent 循环 | 群里 @ 了也不会真正问模型 |
| P0 | 配置前缀不一致：代码 `zone.ai.channel`，配置文件 `zone.channel` | 即使打开开关，通道也可能绑不上配置 |
| P0 | 企微 BotID/Secret、BeeCloud Cookie、LLM Key 写死或空 | 无法稳定上线 |
| P1 | RAG 未挂到回答路径；知识只有枚举，没有文档切分入库 | 「项目知识问答」名不副实 |
| P1 | 无读代码 Tool（白名单仓库 / grep / 读文件） | 不能当研发助手定位代码 |
| P1 | 无只读 SQL Tool | 不能核对业务数据 |
| P2 | 无改代码 / 分支 / PR / 审计状态机 | 「替身改代码」不存在 |
| P2 | 无对话审计日志、权限白名单、限流 | 群里误用风险高 |
| P3 | MCP Client（连别人的 MCP）仍是占位 | 不影响「自己当 Server」 |
| P3 | 飞书未实现 | 不阻塞 |

### 3.3 已知坑（必须先修）

1. **配置前缀**：`ChannelProperties` 为 `zone.ai.channel`，`application.properties` 为 `zone.channel.*`，必须统一。  
2. **Tool 死循环**：历史问题是 URL `build(true)` 抛错后模型无限重试；Agent 必须设 **最大 Tool 轮次** 和超时 `finish=true`。  
3. **BeeCloud 鉴权**：Cookie 过期会返回登录页 HTML，需配置化并明确报错。  
4. **Maven / IDEA 内存**：本机约束 IDEA `-Xmx=2g`，Maven `1g`，禁止把堆改回 4g+。AI 跑测试也要遵守。

---

## 4. 模块落点（建议仍在 `zone-ai-business`）

第一期不拆独立 `zone-channel` 模块，避免跨模块联调拖慢 MVP。目录建议：

```
zone-ai-business/src/main/java/.../business/
  channel/          # 已有：企微 / 飞书通道，只替换 Handler
  agent/            # 新建：编排、记忆、权限、流式对接
  rag/              # 扩展：文档切分入库 + 检索顾问（可从 db/vec 长出来）
  tool/
    CodeAssistantTool.java   # 已有日志
    RepoReadTool.java        # 新建：只读搜代码 / 读文件
    SqlQueryTool.java        # 新建：只读 SELECT
    CodeChangeTool.java      # 新建：补丁 + 审计分支，禁止推 main
  audit/            # 新建：审计单、PR 状态、企微通知
```

职责边界：

- **channel**：只负责「收消息、回消息」，不感知 RAG / SQL。  
- **agent**：只依赖 `ChannelInboundMessage` + `ChannelReplySink` + Tools。  
- **tool**：每个工具最小权限；改代码与查日志拆开。  
- **audit**：人机边界，AI 只创建审计单，人合并。

---

## 5. Agent 运行约定

### 5.1 系统提示（要点）

- 你是供应链金融研发助手，不是万能客服。  
- 先检索知识库，再决定是否调日志 / 代码 / SQL。  
- 不知道就说不知道，禁止编造状态码、金额、接口路径。  
- 回答尽量带出处：文档名、类名、SQL 条件、traceId。  
- **改代码必须走审计 Tool**，禁止声称「已经发到生产」。  
- 日志和 SQL 结果要摘要，不要把原始大报文贴进群。

### 5.2 工具路由（写进 prompt，也写进产品说明）

| 用户问题类型 | 优先工具 |
|---|---|
| 状态码、产品规则、接口含义 | RAG |
| 报错、traceId、某环境刚失败 | BeeCloud 日志 → 必要时读代码 |
| 「这段逻辑在哪」「谁写的校验」 | 搜代码 / 读文件 |
| 「库里有没有这笔」「今天成功几笔」 | 只读 SQL |
| 「帮我改一下文案 / 修 NPE」 | 补丁预览 → 审计分支 / PR |

### 5.3 会话与权限

| 项 | 第一期约定 |
|---|---|
| 会话 Key | `chatid + userid` |
| 记忆 | 内存 LRU，最近 10 轮；后续再落库 |
| 普通成员 | 只能问答 + 只读工具 |
| 改代码 | `userid` 白名单 |
| 群 | 仅处理企微推送的 @ 消息（长连接一般已过滤） |
| Tool 轮次 | 单次对话最多 8 轮 |
| 超时 | 先回「正在分析…」；总超时 120s 必须结束流式 |

### 5.4 流式回复

企微长连接：同一回调 `req_id` + 同一 `stream.id`，多次 `finish=false`，最后 `finish=true`。  
异常、超时、Tool 失败都必须 `finish=true`，避免群里气泡一直转圈。

---

## 6. 分阶段实施计划

原则：**先能在群里正确回答，再给只读工具，最后才给「改代码」，且永远不能直接上生产。**

人员假设：1 名熟悉 zone / 供应链金融的后端，可兼职。日历按人周估算，可压缩但不要跳过审计阶段。

---

### 阶段 0：打底（约 2～3 天）

**目的**：通道真正转起来，排除配置和密钥问题。

**任务**

1. 统一配置前缀（`zone.ai.channel` 与 `application.properties` 一致）。  
2. 填写企微 `bot-id` / `secret`，打开 `enabled`。  
3. Echo 联调：群里 @ 能回「收到 xxx」。  
4. BeeCloud Cookie、LLM Key、Qdrant Key 迁出仓库，改环境变量或本地未提交配置。  
5. 确认 Security 不影响出站 WS；若 Cursor 要用 MCP，再放行 `/sse`、`/mcp/**`。

**验收**

- [ ] 启动日志出现 WeiXin 订阅成功 / `READY`  
- [ ] 群里 @ 必有回复，日志有 `userid`、`chatid`  
- [ ] 关闭开关后进程不再建连  

---

### 阶段 1：群里真正问大模型（约 1～2 周）

**目的**：把 Echo 换成 Agent，先不追求工具齐全。

**任务**

1. 新增 `AgentChannelMessageHandler`（`@Primary` 或替换默认 Echo）。  
2. `ChatClient.builder(chatModel)`，流式输出接到 `ChannelReplySink.replyStream`。  
3. 先挂上已有 `AppLogSearchTool`（可选：本阶段也可先纯对话）。  
4. 会话记忆、Tool 轮次上限、超时强制结束。  
5. 系统提示按 5.1 落地。

**验收**

- [ ] 群里闲聊 / 业务问题有模型回答，而不再是「收到：…（默认 echo）」  
- [ ] 超时或模型失败时气泡会结束，并有友好错误句  
- [ ] 同一用户连续追问能带上最近上下文  

---

### 阶段 2：知识库真正可用（约 1 周，可与阶段 1 并行后半）

**目的**：Qdrant 从「枚举演示」变成「项目知识库」。

**任务**

1. 文档入库流水线（可先脚本，再管理接口）：  
   - 格式：Markdown、接口说明、表结构、状态机说明  
   - 切分：约 512～1024 token，overlap 约 100  
   - metadata：`source`、`module`（如 finance）、`doc_type`、`version`  
2. 回答路径挂检索：`similaritySearch(topK=5～8)` 或 Spring AI `QuestionAnswerAdvisor`。  
3. 检索为空时明确说「知识库没有」，禁止编造。  
4. 文档变更后重新 embed（可先手工跑）。

**建议首批入库内容**

- 融资状态枚举及流转说明（已有 `FinancingStatusEnum` 可继续）  
- 核心接口：申请 / 审批 / 放款 / 还款  
- 常见失败原因与对应日志关键字  
- 关键表：融资单、流水、资方、合同（只描述，不入库真实数据）

**验收**

- [ ] 「融资成功状态码是什么」能命中知识库并答对  
- [ ] 问知识库没有的内容，会承认没有，而不是瞎编接口名  

---

### 阶段 3：只读排障三件套（约 2 周）

**目的**：日志（加固）+ 读代码 + 查库，全部只读。

#### 3.1 BeeCloud 日志（加固现有 Tool）

- Cookie / token 配置化，过期返回明确错误（禁止把 HTML 丢给模型）。  
- `env` 白名单：默认仅 `dev` 及指定测试环境。  
- 限制 `maxResults`，脱敏 token、密码、手机号。  
- 返回继续用 slim 结构：`timestamp / service / hostname / message`。

#### 3.2 读代码 Tool（新建，只读）

建议方法：

| 方法 | 作用 | 限制 |
|---|---|---|
| `repo_list` | 列出允许的仓库 | 配置白名单 |
| `code_search` | 关键词 / 符号搜索 | 路径前缀限制，如 `zone-finance/**` |
| `read_file` | 按文件 + 行号范围读取 | 单次最多约 400 行；禁止 `..` |
| `git_log`（可选） | 最近提交摘要 | 只读 |

工作副本建议独立 clone 到沙箱目录（例如 `D:\ai-sandbox\zone`），不要直接在开发者正在改的工作区里乱读未提交文件（可配置「是否允许读工作区」）。

禁止：读 `.env`、`application-lls.properties`、私钥、`id_rsa`。

#### 3.3 只读 MySQL Tool（新建）

- 独立只读账号，仅授权业务库指定表。  
- 只允许单条 `SELECT`；拦截 `;`、注释绕过、`INTO OUTFILE`、`UNION` 视情况收紧。  
- 强制 `LIMIT`（默认 50，最大 200）。  
- 返回列名 + 行数据；行数过多只返回截断说明。

**验收**

- [ ] 真实 traceId 能查出日志摘要  
- [ ] 「某校验在哪个类」能给出文件路径  
- [ ] 「某资方今天成功几笔」SQL 结果可人工抽查  
- [ ] `DROP TABLE` / 读 `../` / 无白名单表 → 明确拒绝  

---

### 阶段 4：多工具编排（约 1～2 周）

**目的**：一次提问能串联 RAG + 日志 + 代码 + SQL。

**任务**

1. 一个 `ChatClient` 同时注册上述 Tools。  
2. 对话落库：`question / tools_called / answer / userid / chatid / latency`。  
3. 企微回复 Markdown；工具原始输出只进模型上下文，进群必须摘要。  
4. 失败可观测：哪个 Tool 失败、参数是什么（脱敏后）。

**验收用例（供应链金融）**

1. 「dev 上 traceId=xxx 这笔融资为什么失败？」  
   → 日志 → 对照状态枚举 / 代码分支 → 给出可能原因和文件位置。  
2. 「待审核停留超过 1 小时一般是什么原因？」  
   → 知识库 + 可选查库统计。  
3. 「放款成功写回是哪个 Listener？」  
   → 搜代码定位。

---

### 阶段 5：改代码必须审计（约 2～3 周）

**目的**：AI 可以改代码，但 **你不点通过就永远上不了主分支**。

#### 5.1 硬规则（产品 + 代码双重限制）

- 禁止 `push` 到 `main` / `master` / `release`。  
- 禁止 `--force`、禁止改 git config、禁止跳过 hook。  
- 禁止在开发者日常工作区直接改文件；只改 **沙箱 worktree**。  
- 单次 PR：文件数上限（建议 ≤ 8）、diff 行数上限（建议 ≤ 400）。  
- 第一期允许改的范围建议仅 `zone-finance` 指定包；禁止改 gateway 证书、全局 BOM 版本。  
- 核心资金状态机、放款回调：第一期 **只读不改**，或必须双人审计。

#### 5.2 Tool 流程

```
用户：「把某提示文案改一下」
    → propose_patch     预览 diff，不落盘
    → （白名单用户确认）apply_patch   写入沙箱
    → run_tests         只跑相关模块，Maven -Xmx1g
    → open_audit_request
         新分支 ai/fix-yyyyMMdd-HHmm-xxx
         commit（message 标明 AI 生成 + 原因）
         push 到 origin 该分支
         开 PR
         企微发卡片：PR 链接、文件列表、测试结果
    → 状态 DRAFT → WAITING_AUDIT
    → 你在 GitHub Review Merge 或点卡片「通过/驳回」
    → APPROVED 后由人（或受保护的 CI）合并；AI 不执行合 main
```

#### 5.3 审计方式（选一个做透）

| 方式 | 优点 | 建议 |
|---|---|---|
| GitHub / GitLab PR | 权限、CI、评论最完整 | **首选** |
| 企微模板卡片通过/驳回 | 群里闭环快 | 可作为通知，真正合并仍走 PR |

#### 5.4 验收

- [ ] 小改动能产出 PR，main 在你合并前无变化  
- [ ] 让 AI 推 main / 改生产配置 / force push → 拒绝并在群里说明  
- [ ] 单测失败时 PR 仍可开，但卡片标明失败，默认不能合  

---

### 阶段 6：生产化与扩展（约 1～2 周，可穿插）

1. 限流：每群每分钟问答次数、每用户每天改代码次数。  
2. 密钥轮换：BeeCloud、LLM、只读 DB、企微 Secret。  
3. 指标：成功率、Tool 失败率、PR 驳回率、P95 耗时。  
4. 飞书：实现 `FeishuBotClient`，复用同一 `ChannelMessageHandler`。  
5. MCP Client：仅当需要调用外部 MCP 时再填 `McpServerConfigServiceImpl.doStart()`。  
6. 多模态：群里发截图排障（需网关视觉模型），与文生图不是同一条链路。

---

## 7. 建议排期总表（单人）

| 周次 | 阶段 | 可交付 |
|---|---|---|
| 第 1 周前半 | 0 | 配置修好，Echo 群聊打通 |
| 第 1～2 周 | 1 | 群里能问大模型（流式） |
| 第 2～3 周 | 2 | RAG 挂上，核心文档可问 |
| 第 3～4 周 | 3 | 日志加固 + 读代码 |
| 第 4～5 周 | 3 | 只读 MySQL |
| 第 5～6 周 | 4 | 三工具串联真实排障 |
| 第 7～9 周 | 5 | 改代码 + PR 审计 |
| 第 10～12 周 | 6 | 权限、限流、观测、飞书可选 |

**前 4～6 周**即可作为「群里的排障 / 知识助手」上线。  
**替身改代码**必须放在审计机制之后，否则金融业务风险不可控。

---

## 8. 安全与合规（供应链金融）

| 风险 | 控制措施 |
|---|---|
| 编造放款结果 / 状态机 | RAG 强制引用；核心状态机第一期只读 |
| SQL 拖库、注入 | 只读账号 + 表白名单 + 仅 SELECT + LIMIT |
| 密钥进群、进 Git | 工具输出脱敏；禁止读密钥文件；配置不入库 |
| 群成员乱 @ 导致乱改生产 | 改代码白名单；永不 push main |
| Tool 无限循环 | max rounds + 超时 finish |
| 错误代码进入资金链路 | PR + 单测 + 人工 Merge；状态机/放款代码提高审计级别 |
| 多实例互踢企微长连接 | 同一 Bot 同时仅一条连接；K8s replicas=1 或 Redis 选主 |

本机环境约束（必须遵守）：

- IDEA `-Xmx=2g`，Maven importer/runner `1g`  
- 不把堆改回 4g+  
- 源码与 Maven 使用 UTF-8  

---

## 9. 配置清单（实施时填写，勿把密钥提交进 Git）

```properties
# 通道（前缀需与 ChannelProperties 保持一致）
zone.ai.channel.enabled=true
zone.ai.channel.weixin.enabled=true
zone.ai.channel.weixin.bot-id=${WEIXIN_BOT_ID}
zone.ai.channel.weixin.secret=${WEIXIN_BOT_SECRET}

# 大模型 / 向量库已有 spring.ai.* ，密钥改环境变量

# 只读库（阶段 3）
zone.ai.tool.sql.url=
zone.ai.tool.sql.username=
zone.ai.tool.sql.password=
zone.ai.tool.sql.allowed-tables=financing_order,financing_flow

# 代码沙箱（阶段 3～5）
zone.ai.tool.repo.root=D:/ai-sandbox/zone
zone.ai.tool.repo.allow-paths=zone-finance/**

# 审计
zone.ai.audit.git-remote=origin
zone.ai.audit.forbidden-branches=main,master,release
zone.ai.audit.approver-userids=
```

---

## 10. 测试与验收清单（汇总）

### 通道

- [ ] 订阅成功、断网重连、第二实例不抢连（或明确互踢）  
- [ ] 进入会话欢迎语 5s 内发出  

### Agent

- [ ] Echo 已替换；流式完整结束  
- [ ] 工具失败不会把堆栈原文刷屏  

### RAG

- [ ] 融资状态类问题命中知识库  
- [ ] 未知问题承认缺失  

### 只读工具

- [ ] 日志 / 代码 / SQL 正反向用例（允许 + 拒绝）  

### 审计改代码

- [ ] PR 产生且 main 不变  
- [ ] 危险操作全部拒绝  

---

## 11. 近期下一步（最短路径）

按下面三件事做，群里就能先看到「智能」，而不是 Echo：

1. **修好 `zone.ai.channel` 与配置文件前缀，打开企微长连接。**  
2. **实现 `AgentChannelMessageHandler`：ChatClient 流式 + 现有 `beecloudSearchLogs`。**  
3. **把 Qdrant 检索挂进 Agent（先用已有融资状态，同时开始切分业务文档）。**

完成这三步后再开读代码 / SQL；改代码放到有 PR 审计之后。

---

## 12. 文档修订

| 日期 | 说明 |
|---|---|
| 2026-08-14 | 初稿：基于当前 zone-ai 代码现状整理缺口与分期计划 |
