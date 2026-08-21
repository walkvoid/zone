# jinkoscf-batch 服务知识库

## 1. 服务概述
- **服务名称**: jinkoscf-batch（应用名：acflow-batch-web）
- **业务定位**: 供应链金融批量任务服务，负责定时/批量调度，包括融资结果轮询、兑付流程、银行对账、影像与发票同步、企业同步等
- **核心价值**: 通过 XXL-JOB 定时任务调度平台，自动化执行各类批量业务处理，保障融资交易、对账、通知等业务流程的自动运转

## 2. 核心功能模块

### 2.1 银行融资结果轮询
- **功能描述**: 定时轮询各银行融资申请结果，覆盖招商、交行、北京、邮储、广发、浦发、渤海、民生、中信、工行等银行
- **关键文件**:
  - `jinkoscf-batch-service/src/main/java/com/lls/cloud/acflow/batch/service/jobhandle/bank/CmbchinaBankHandler.java`（招商银行）
  - `jinkoscf-batch-service/.../bank/BocomBankHandler.java`（交通银行）
  - `jinkoscf-batch-service/.../bank/BobBankHandler.java`（北京银行）
  - `jinkoscf-batch-service/.../bank/PsbcBankHandler.java`（邮储银行）
  - `jinkoscf-batch-service/.../bank/SpdBankHandler.java`（浦发银行）
  - `jinkoscf-batch-service/.../bank/CbhbBankHandler.java`（渤海银行）
  - `jinkoscf-batch-service/.../bank/CmbcBankHandler.java`（民生银行）
  - `jinkoscf-batch-service/.../bank/CiticBankHandler.java`（中信银行）
  - `jinkoscf-batch-service/.../bank/IcbcBankHandler.java`（工商银行）
  - `jinkoscf-batch-service/.../bank/AbcBankHandler.java`（农业银行）

### 2.2 资产兑付（AST Due Pay）
- **功能描述**: 到期资产兑付全流程，包括兑付初始化、在途通知、执行、失败告警、校验
- **关键文件**:
  - `jinkoscf-batch-service/.../jobhandle/paytrade/AstDuePayInitJobHandler.java`
  - `jinkoscf-batch-service/.../jobhandle/paytrade/AstDuePayMailJobHandler.java`
  - `jinkoscf-batch-service/.../jobhandle/paytrade/AstOverDuePayNoticeJobHandler.java`
  - `jinkoscf-batch-service/.../jobhandle/paytrade/PayTradeNoticeJobHandler.java`

### 2.3 对账与清分
- **功能描述**: 交易对账、广发对账推送、中信清分等
- **关键文件**:
  - `jinkoscf-batch-service/.../jobhandle/ReconciliationJobHandler.java`
  - `jinkoscf-batch-service/.../jobhandle/CgbReconciliationPushJobHandler.java`

### 2.4 影像与发票
- **功能描述**: 影像重试、发票验真、电子档案处理
- **关键文件**:
  - `jinkoscf-batch-service/.../jobhandle/MediaListenerJobHandler.java`
  - `jinkoscf-batch-service/.../jobhandle/InvoiceTrueMediaRefreshJobHandler.java`
  - `jinkoscf-batch-service/.../jobhandle/InvoiceFalseHandler.java`
  - `jinkoscf-batch-service/.../jobhandle/ElectronicArchivesHandler.java`

### 2.5 费率配置
- **功能描述**: 最低费率、平台费率的定时生效与过期处理
- **关键文件**:
  - `jinkoscf-batch-service/.../jobhandle/MinRateConfigJobHandler.java`
  - `jinkoscf-batch-service/.../jobhandle/PlatformFeeMinRateJobHandler.java`

### 2.6 SAP 对接
- **功能描述**: 核心企业编号、供应商编号同步
- **关键文件**:
  - `jinkoscf-batch-service/.../jobhandle/SapQueryCoreCodeHandler.java`
  - `jinkoscf-batch-service/.../jobhandle/SapQuerySupCodeHandler.java`

### 2.7 IAM 同步
- **功能描述**: 用户组织、人员状态、部门信息同步
- **关键文件**:
  - `jinkoscf-batch-service/.../jobhandle/IamUserOrgJobHandler.java`
  - `jinkoscf-batch-service/.../jobhandle/IamPersonStatusHandler.java`
  - `jinkoscf-batch-service/.../jobhandle/IamDepartmentSyncHandler.java`

### 2.8 运营与通知
- **功能描述**: 终止无效流程、批量推送企业、融资邮件通知、证书签署等
- **关键文件**:
  - `jinkoscf-batch-service/.../jobhandle/OpsInvalidFlowJobHandler.java`
  - `jinkoscf-batch-service/.../jobhandle/PushCustBuildToOpssHandler.java`
  - `jinkoscf-batch-service/.../jobhandle/CreditAndCashSendEmailJobHandler.java`
  - `jinkoscf-batch-service/.../jobhandle/CreateCertAndSignJobHandler.java`

## 3. 关键接口清单

本服务不提供 HTTP 接口，所有能力通过 XXL-JOB 定时/手动触发。主要 Job Handler：

| Job Handler | 功能说明 | 所属类 |
|------------|----------|--------|
| astDuePayInitJobHandler | 兑付初始化 | AstDuePayInitJobHandler |
| astDuePayJobHandler | 兑付执行 | AstDuePayInitJobHandler |
| astDuePayOnWayNoticeJobHandler | 在途兑付通知 | AstDuePayInitJobHandler |
| astDuePayAlarmJobHandler | 兑付告警 | AstDuePayInitJobHandler |
| cmbchinaCashResultHandler | 招商融资结果 | CmbchinaBankHandler |
| bocomCashStatusQuery | 交行融资状态 | BocomBankHandler |
| bobCashResultHandler | 北京银行融资结果 | BobBankHandler |
| psbcCashResultHandler | 邮储融资结果 | PsbcBankHandler |
| spdCashResultHandler | 浦发融资结果 | SpdBankHandler |
| cbhbCashResultHandler | 渤海融资结果 | CbhbBankHandler |
| cmbcCashResultHandler | 民生融资结果 | CmbcBankHandler |
| queryCashResult | 工行融资结果 | IcbcBankHandler |
| reconciliationHandle | 中信对账 | CiticBankHandler |
| ReconciliationJobHandler | 对账任务 | ReconciliationJobHandler |
| cgbReconciliationPushJobHandler | 广发对账推送 | CgbReconciliationPushJobHandler |
| mediaListenerJobHandler | 影像监听重试 | MediaListenerJobHandler |
| sapQueryCoreCodeHandler | SAP核心企业编号 | SapQueryCoreCodeHandler |
| platFeeOpenBillJobHandler | 平台费开票 | PlatFeeOpenBillJobHandler |
| feeAllocateDataSyncHandler | 费用分摊同步 | TsFeeAllocateDataSyncHandler |
| IamUserOrgJobHandler | IAM用户组织 | IamUserOrgJobHandler |
| cashStateQueryJobHandler | 融资状态查询 | CashStateQueryJobHandler |

## 4. 依赖关系

### 4.1 依赖的服务
| 服务/接口 | 用途 |
|----------|------|
| jinkoscf-transaction-api | 交易资产、交易配置 |
| jinkoscf-workflow-api | 工作流 |
| jinkoscf-gateway-api | 银行对接 Provider |
| jinkoscf-business-common-api | 额度、客户、发票、合同 |
| jinkoscf-abs-api | ABS 相关 |
| jinkoscf-factor-api | 保理相关 |
| bccp-batch-service/api | 批量任务基座 |

### 4.2 被依赖的服务
本服务为定时任务执行器，不对外提供 Dubbo/HTTP 接口，不被其他服务直接依赖。

## 5. 数据模型

本服务不直接定义数据库实体，数据存储由其他微服务负责。主要 DTO：

| DTO 类名 | 用途 |
|----------|------|
| AstDuePayInitDTO | 代付初始化参数（起止日期、节假日、是否手动） |
| PayTradeImportDTO | 资产代付导入（资产编号、凭证、付款账户、金额） |
| PayNotSuccessDTO | 兑付不成功导出 |
| DistribNotSuccessDTO | 银行清分不成功导出 |
| AstPayEmailTemplateParamDTO | 代付邮件模板参数 |

## 6. 技术要点
- **XXL-JOB**: 所有批量任务通过 `@XxlJob` 注解定义，由 XxlJobHelper 获取参数和记录日志
- **Nacos 配置**: dataIds 为 `jinkoscf-batch`、`jinkoscf-common`
- **Facade 封装**: 统一封装 Dubbo 调用，如 DuePayNoticeFacade、CustForPaymentFacade、TsTransactionFacade
- **日志追踪**: XxlJobLogTraceIdAspect 为 XxlJob 增加 traceId
- **异步执行**: 部分流程使用 CompletableFuture 和 batchExecutor 异步处理
- **消息通知**: 通过 Dubbo 调用 Message/Notice 服务发送站内信、短信、邮件
