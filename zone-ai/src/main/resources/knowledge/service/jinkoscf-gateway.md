# jinkoscf-gateway 服务知识库

## 1. 服务概述
- **服务名称**: jinkoscf-gateway（应用名：acflow-gateway-web）
- **业务定位**: 供应链金融网关服务，承担银行对接、开放接口、OPSS运营平台对接、晶科司库回调、消息模板等对外能力
- **核心价值**: 作为系统与外部银行、运营平台、晶科集团系统之间的桥梁，统一管理多银行对接、回调处理、事件监听和消息推送

## 2. 核心功能模块

### 2.1 银行对接（多银行 Provider）
- **功能描述**: 对接 13+ 家银行的融资、准入、回单、合同等业务，通过 Dubbo Provider 暴露银行操作能力
- **关键文件**:
  - `jinkoscf-gateway-provider/.../impl/BankofdlBankProviderImpl.java`（大连银行）
  - `jinkoscf-gateway-provider/.../impl/BobBankProviderImpl.java`（北京银行）
  - `jinkoscf-gateway-provider/.../impl/BocomManualProviderImpl.java`（交通银行）
  - `jinkoscf-gateway-provider/.../impl/IcbcBankProviderImpl.java`（工商银行）
  - `jinkoscf-gateway-provider/.../impl/CibBankProviderImpl.java`（兴业银行）
  - `jinkoscf-gateway-provider/.../impl/SpdBankProviderImpl.java`（浦发银行）
  - `jinkoscf-gateway-provider/.../impl/CiticBankProviderImpl.java`（中信银行）
  - `jinkoscf-gateway-provider/.../impl/CmbcBankProviderImpl.java`（民生银行）
  - `jinkoscf-gateway-provider/.../impl/CbhbBankProviderImpl.java`（渤海银行）
  - `jinkoscf-gateway-provider/.../impl/AbcBankProviderImpl.java`（农业银行）
  - `jinkoscf-gateway-provider/.../impl/CgbBankProviderImpl.java`（广发银行）
  - `jinkoscf-gateway-provider/.../impl/PsbcBankProviderImpl.java`（邮储银行）
  - `jinkoscf-gateway-provider/.../impl/CmbchinaBankProviderImpl.java`（招商银行）

### 2.2 银行回调服务
- **功能描述**: 接收各银行的异步回调通知，通过 fn-open-api 框架暴露回调端点
- **关键文件**:
  - `jinkoscf-gateway-open-web/.../abc/AbcCallbackService.java`（农行回调）
  - `jinkoscf-gateway-open-web/.../bankofdl/BankofdlCallbackService.java`（大连银行回调）
  - `jinkoscf-gateway-open-web/.../cgb/CgbCallbackService.java`（广发回调）
  - `jinkoscf-gateway-open-web/.../psbc/PsbcCallBackService.java`（邮储回调）

### 2.3 银行通知监听器（Spring Event）
- **功能描述**: 通过 Spring Event 机制监听银行通知事件，处理融资结果、客户准入、文件通知等
- **关键文件**:
  - `jinkoscf-gateway-open-web/.../listener/BobCashNoticeListener.java`（北京银行融资通知）
  - `jinkoscf-gateway-open-web/.../listener/BocomNoticeListener.java`（交行融资结果）
  - `jinkoscf-gateway-open-web/.../listener/BocomCustNoticeListener.java`（交行客户准入）
  - `jinkoscf-gateway-open-web/.../listener/BocomFileNoticeListener.java`（交行文件通知）
  - `jinkoscf-gateway-open-web/.../listener/IcbcCashNoticeListener.java`（工行融资结果）
  - `jinkoscf-gateway-open-web/.../listener/CibCashNoticeListener.java`（兴业融资结果）
  - `jinkoscf-gateway-open-web/.../listener/SpdCashNoticeListener.java`（浦发融资结果）
  - `jinkoscf-gateway-open-web/.../listener/CashRetNoticeListener.java`（中信融资结果）
  - `jinkoscf-gateway-open-web/.../listener/CustCreateNoticeListener.java`（中信客户准入）
  - `jinkoscf-gateway-open-web/.../listener/ReconciliationNoticeListener.java`（中信对账）
  - `jinkoscf-gateway-open-web/.../listener/CmbcCustomerNoticeListener.java`（民生客户准入）
  - `jinkoscf-gateway-open-web/.../listener/CmbcLoanNoticeListener.java`（民生融资）
  - `jinkoscf-gateway-open-web/.../listener/CbhbNoticeListener.java`（渤海融资结果）
  - `jinkoscf-gateway-open-web/.../listener/CbhbContractPushEventListener.java`（渤海合同推送）

### 2.4 OPSS 运营平台对接
- **功能描述**: 对接运营平台的资产、客户、媒体事件，处理资产材料校验、客户审核、风控校验
- **关键文件**:
  - `jinkoscf-gateway-open-web/.../opss/OpssAssetEventListener.java`（资产事件）
  - `jinkoscf-gateway-open-web/.../opss/AcflowCustEventListener.java`（客户事件）
  - `jinkoscf-gateway-open-web/.../opss/AcflowMediaEventListener.java`（媒体事件）

### 2.5 晶科司库回调
- **功能描述**: 接收晶科司库系统的凭证开立、支付结果、FSSC开票等回调
- **关键文件**:
  - `jinkoscf-gateway-web/.../controller/JinkoCallBackController.java`
  - `jinkoscf-gateway-provider/.../impl/JinkoCallBackProviderImpl.java`

### 2.6 消息推送
- **功能描述**: 钉钉工作通知、短信/邮件模板管理、验证码下发
- **关键文件**:
  - `jinkoscf-gateway-service/.../dingtalk/DingTalkWorkMessageService.java`
  - `jinkoscf-gateway-web/.../controller/MessageTemplateController.java`
  - `jinkoscf-gateway-service/.../message/FactoringIndentifyCodeServiceImpl.java`

### 2.7 中登登记
- **功能描述**: 应收账款转让登记，对接中登网
- **关键文件**:
  - `jinkoscf-gateway-web/.../controller/ThirdBusinessZhongdengController.java`

### 2.8 自动校验
- **功能描述**: 立项自动校验、融资自动校验
- **关键文件**:
  - `jinkoscf-gateway-web/.../controller/InitiationAutoCheckController.java`
  - `jinkoscf-gateway-web/.../controller/LoanAutoCheckController.java`

## 3. 关键接口清单

| 接口路径 | 方法 | 功能说明 | 所属Controller |
|----------|------|----------|---------------|
| /openapi/jinko/assetApply | POST | 凭证开立申请 | JinkoCallBackController |
| /openapi/jinko/assetResult | POST | 开立凭证结果查询 | JinkoCallBackController |
| /openapi/jinko/paymentResultSync | POST | 支付结果同步 | JinkoCallBackController |
| /openapi/jinko/getFSSC | POST | 获取FSSC数据 | JinkoCallBackController |
| /openapi/jinko/assetAbolishSync | POST | 推动作废通知到司库 | JinkoCallBackController |
| /openapi/jinko/createOtherBillResultSync | POST | FSSC开票结果回调 | JinkoCallBackController |
| /messageTemplate/msgTemplate | POST | 短信模板 | MessageTemplateController |
| /messageTemplate/emailTemplate | POST | 邮件模板 | MessageTemplateController |
| /messageTemplate/testSftpConn | POST | SFTP连接测试 | MessageTemplateController |
| /register/autoRegister | POST | 自动中登登记 | ThirdBusinessZhongdengController |
| /register/checkAlreadyRegister | POST | 校验已登记 | ThirdBusinessZhongdengController |
| /register/getZhongdengQuery | GET | 中登查询 | ThirdBusinessZhongdengController |
| /autocheck/* | POST | 立项自动校验 | InitiationAutoCheckController |
| /loan/autocheck/* | POST | 融资自动校验 | LoanAutoCheckController |

## 4. 依赖关系

### 4.1 依赖的服务
| 服务/组件 | 用途 |
|----------|------|
| jinkoscf-transaction-api | 交易相关 |
| jinkoscf-business-common-api | 业务公共 |
| jinkoscf-tools-api | 工具服务 |
| bccp-thirdparty-manager | 第三方管理 |
| bccp-opss / bccp-ocr / bccp-sign | 运营/OCR/签章 |
| fn-bocom/cmbc/spd/citic/cib/cbhb/icbc/bob/abc/cgb/bankofdl/psbc SDK | 各银行SDK |
| fn-open-api-boot-starter | 开放接口框架 |
| operation-mid-sdk-api | 小微蜂运营中台 |

### 4.2 被依赖的服务
- jinkoscf-batch（调用银行 Provider 进行融资轮询）
- jinkoscf-transaction（调用银行 Provider 进行融资申请）
- jinkoscf-workflow（调用银行 Provider 进行合同签署）

## 5. 数据模型

| DO/DTO 类名 | 说明 |
|-------------|------|
| RetryTaskDO | 重试任务 |
| BankMediaUploadRecordDO | 银行影像上传记录 |
| PubOpenInvokeLogDO | 开放接口调用日志 |
| AutoCheckCertificationInfoDO | 自动校验认证信息 |
| ZhongDengReceivableDTO | 中登应收 |
| ZhongDengInvoiceDTO | 中登发票 |
| BocomFileDTO | 交行文件 |
| CiticCashTradeReceiptFileDTO | 中信回单文件 |

## 6. 技术要点
- **多银行对接**: 通过各银行 SDK + Spring Event + Listener 模式处理银行异步回调
- **fn-open-api**: 回调服务通过 fn-open-api-boot-starter 暴露，路径形如 `/openapi/v1/{bank}/**`
- **签名校验**: ReqSignVerifyAspect 对开放接口做签名校验
- **OPSS 事件**: 通过 Dubbo 暴露 AssetEventListener、MediaEventListener、CustEventListener
- **分布式锁**: 部分场景使用 @Lock 注解实现分布式锁
- **日志**: @RpcLog 记录 RPC 调用，fn-http-log-boot-starter 记录 HTTP 日志
- **钉钉集成**: DingTalkWorkMessageService 发送钉钉工作通知（文本、卡片）
- **晶科集成**: 司库、SAP、FSSC、DBASS、电子档案、IAM/OA/SSO 等通过 Feign 或外部客户端调用
