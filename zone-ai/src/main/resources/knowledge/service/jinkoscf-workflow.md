# jinkoscf-workflow 服务知识库

## 1. 服务概述
- **服务名称**: jinkoscf-workflow（应用名：acflow-workflow-web）
- **业务定位**: 供应链金融工作流服务，基于 Activiti 5.22（lls-wkfl 封装）的流程引擎，负责融资、提前付款、资产签发、建档、作废、转让、费用分润、报价等业务的流程编排与执行
- **核心价值**: 统一管理各业务流程的生命周期，通过 ExecutionListener 和 TaskListener 机制驱动业务流转，支持自动化审批和合同签署

## 2. 核心功能模块

### 2.1 融资流程（Cash）
- **功能描述**: 多级流转融资主流程，包括供应商/核心/财务合同签署、付息、放款审核、凭证拆分等
- **关键文件**:
  - `jinkoscf-workflow-provider/.../cash/AutoTaskHandler.java`
  - `jinkoscf-workflow-provider/.../cash/CashDataFacade.java`
  - `jinkoscf-workflow-provider/.../cash/CashSpySignContractTaskListenerHandler.java`
  - `jinkoscf-workflow-provider/.../cash/CashCoreSignContractTaskListenerHandler.java`
  - `jinkoscf-workflow-provider/.../cash/CashFinanceSignContractTaskListenerHandler.java`

### 2.2 提前付款流程（Prepay）
- **功能描述**: 提前付款流程，包括供应商/总行审核、合同签署、放款策略等
- **关键文件**:
  - `jinkoscf-workflow-provider/.../prepay/PrepayAutoTaskHandler.java`
  - `jinkoscf-workflow-provider/.../prepay/PrepayDataFacade.java`
  - `jinkoscf-workflow-provider/.../prepay/PrepayAgwAuditTaskListenerHandler.java`
  - `jinkoscf-workflow-provider/.../prepay/PrepaySikuPayHandler.java`

### 2.3 资产签发流程（Issue）
- **功能描述**: 资产签发主/子流程，包括确权、风控、签收、数据上传等
- **关键文件**:
  - `jinkoscf-workflow-provider/.../issue/IssueCommonExecutionHandler.java`
  - `jinkoscf-workflow-provider/.../issue/IssueCreateTsAssetExecutionHandler.java`
  - `jinkoscf-workflow-provider/.../issue/IssueTsAssetSignExecutionHandler.java`
  - `jinkoscf-workflow-provider/.../issue/IssueOpsAuditExecutionHandler.java`

### 2.4 建档流程（Cust）
- **功能描述**: 企业建档审核流程，包括一审、二审、采购员处理
- **关键文件**:
  - `jinkoscf-workflow-provider/.../cust/CustPassAutoTaskHandler.java`
  - `jinkoscf-workflow-provider/.../cust/CustPurchasingAutoTaskHandler.java`
  - `jinkoscf-workflow-provider/.../cust/ApplyCustCompanyInfoFacade.java`

### 2.5 作废流程（Abolish）
- **功能描述**: 凭证作废申请与审核流程
- **关键文件**:
  - `jinkoscf-workflow-provider/.../abolish/AbolishAutoTaskHandler.java`
  - `jinkoscf-workflow-provider/.../abolish/AbolishFacade.java`

### 2.6 转让流程（Transfer）
- **功能描述**: 转让发起/签收、合同签署流程
- **关键文件**:
  - `jinkoscf-workflow-provider/.../transfer/SignByReceiverExecutionListenerHandler.java`
  - `jinkoscf-workflow-provider/.../transfer/SignByLaunchExecutionListenerHandler.java`
  - `jinkoscf-workflow-provider/.../transfer/TransactionFacade.java`

### 2.7 费用分润流程（FeeAllocate）
- **功能描述**: 分润开票审批流程
- **关键文件**:
  - `jinkoscf-workflow-provider/.../feeallocate/FeeAllocateAutoTaskHandler.java`
  - `jinkoscf-workflow-provider/.../feeallocate/FeeAllocateDataFacade.java`

### 2.8 报价审批流程（CashBidding）
- **功能描述**: 报价审批通过/拒绝处理
- **关键文件**:
  - `jinkoscf-workflow-provider/.../cashbidding/CashbiddingSucessTaskHandler.java`
  - `jinkoscf-workflow-provider/.../cashbidding/CashbiddingRejectTaskHandler.java`
  - `jinkoscf-workflow-provider/.../cashbidding/CashBiddingFacade.java`

### 2.9 额度审批流程（Limit）
- **功能描述**: 额度初审、二审、合同签署
- **关键文件**:
  - `jinkoscf-workflow-provider/.../limit/LimitFirstApproveTaskListenerHandler.java`
  - `jinkoscf-workflow-provider/.../limit/LimitSecondApproveTaskListenerHandler.java`
  - `jinkoscf-workflow-provider/.../limit/LimitContractFacade.java`

### 2.10 通知管理
- **功能描述**: 待办通知、消息推送
- **关键文件**:
  - `jinkoscf-workflow-provider/.../notice/NoticeInfoFacade.java`
  - `jinkoscf-workflow-provider/.../notice/MessageInfoFacade.java`

## 3. 关键接口清单

| 接口路径 | 方法 | 功能说明 | 所属Controller |
|----------|------|----------|---------------|
| /tsAsset/splitTsAsset | POST | 凭证拆分 | TsAssetController |
| /tsAsset/tsAssetTransFailed | POST | 凭证交易失败 | TsAssetController |
| /tsAsset/releaseCashInvoice | POST | 释放凭证融资发票 | TsAssetController |
| /contract/createCashContractSpy | POST | 生成供应商合同 | ContractController |
| /contract/createCashContractCore | POST | 生成核心企业合同 | ContractController |
| /contract/createCashContractFinace | POST | 生成财务合同 | ContractController |
| /contract/signSingle | POST | 单合同签署 | ContractController |
| /contract/createContractBatch | POST | 批量生成合同 | ContractController |
| /contract/signContractBatch | POST | 批量签署合同 | ContractController |
| /contract/createAndSignContract | POST | 创建并签署合同 | ContractController |
| /jinko/global/log/trace/getTraceLog | GET | 获取跟踪日志 | WkflTraceLogJinkoController |
| /jinko/global/log/trace/getProcessInstDetail | POST | 流程实例明细 | WkflTraceLogJinkoController |
| /workbench/display/getProcinstByBusinessId | POST | 根据业务ID获取流程实例 | AcflowDisplayController |
| /workbench/display/createTsAssetFlowAndPlatSign | POST | 创建凭证流程并平台签署 | AcflowDisplayController |

## 4. 依赖关系

### 4.1 依赖的服务
| 服务/组件 | 用途 |
|----------|------|
| lls-wkfl-api / lls-wkfl-core / lls-wkfl-client | 工作流引擎 |
| jinkoscf-transaction-api | 交易 |
| jinkoscf-factor-api | 保理/资方 |
| jinkoscf-abs-api | ABS |
| bccp-* 系列 | 合同、用户、通知、资产、授信、媒体、签章 |

### 4.2 被依赖的服务
- jinkoscf-batch（调用 WorkflowNewProvider）
- jinkoscf-business-common（调用工作流接口）
- jinkoscf-transaction（调用工作流接口）
- jinkoscf-spypc（调用工作流接口）

## 5. 数据模型

| DO/DTO 类名 | 表/用途 |
|-------------|--------|
| WorkflowProcinstCustDO | 流程实例与客户关系 |
| OaTodoSendLogDO | OA待办发送日志 |
| WfAppnoSeq | 工作流申请单号序列 |
| WorkflowProcinstCustDTO | 流程实例客户信息 |
| WorkflowHiTaskinstDTO | 历史任务实例 |
| NoticeInfoDTO | 通知信息 |
| AutoDelegateResultDTO | 自动委派结果 |

## 6. 技术要点
- **lls-wkfl 3.0.0**: 基于 Activiti 的封装，通过 Handler 模式实现流程节点处理
- **Handler 模式**: IExecutionListenerHandler（执行监听）和 ITaskListenerHandler（任务监听）两种接口
- **@Handler 注解**: 通过 owner、name、targets 指定流程定义 ID 和 elemIds，自动绑定到流程节点
- **Dubbo 远程触发**: Handler 以 @DubboService 暴露，供工作流引擎远程触发
- **Facade 编排**: CashDataFacade、PrepayDataFacade、AbolishFacade 等封装多个 RPC 调用
- **流程变量**: 通过 ExecutionListenerHandlerForm、ExtHandlerResult.variables 传递与回写
- **流程定义**: JSON 格式存储在 jinkoscf-common/.dev-standards/knowledge/procdefs/ 目录
- **流程分组**: wkfl.procdefToNoticeGroup.* 将流程 ID 映射到站内信分组
