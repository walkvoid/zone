# jinkoscf-spypc 服务知识库

## 1. 服务概述
- **服务名称**: jinkoscf-spypc（应用名：acflow-spyPc-web）
- **业务定位**: 供应商PC端Web应用，面向供应商的企业保理、融资、资产与合同管理的综合业务门户
- **核心价值**: 为供应商提供一站式操作界面，涵盖额度查看、客户建档、资产/发票/合同管理、保理融资、ABS资产池、提前付款、转让等全流程操作

## 2. 核心功能模块

### 2.1 额度管理
- **功能描述**: 融资额度、开单额度、已生效/已失效额度统计及分页、额度分配
- **关键文件**:
  - `jinkoscf-spypc-business-common/.../limit/CustomLimitController.java`

### 2.2 客户管理
- **功能描述**: 企业信息维护、营业执照OCR、账户管理、授权管理、建档工作流
- **关键文件**:
  - `jinkoscf-spypc-business-common/.../cust/CustCompanyInfoController.java`
  - `jinkoscf-spypc-business-common/.../cust/BaseCustBuildController.java`

### 2.3 资产管理
- **功能描述**: 资产导入、新增应收账款、重复校验、扩展字段
- **关键文件**:
  - `jinkoscf-spypc-business-common/.../asset/AssetController.java`

### 2.4 发票管理
- **功能描述**: 发票新增验真、上传OCR、绑定、批量操作
- **关键文件**:
  - `jinkoscf-spypc-business-common/.../invoice/InvoiceController.java`

### 2.5 合同管理
- **功能描述**: 合同查看、预览、生成、签署、批量操作
- **关键文件**:
  - `jinkoscf-spypc-business-common/.../contract/ContractController.java`
  - `jinkoscf-spypc-business-common/.../contract/BaseContractController.java`

### 2.6 保理资产（Factor Asset）
- **功能描述**: 买方/卖方资产分页、保存、详情、导出
- **关键文件**:
  - `jinkoscf-spypc-factor/.../FactorAssetController.java`

### 2.7 保理融资（Factor Cash）
- **功能描述**: 首页待融资、融资列表、提交融资、临时保存、材料上传
- **关键文件**:
  - `jinkoscf-spypc-factor/.../FactorCashController.java`

### 2.8 借据与还款
- **功能描述**: 借据分页/统计/详情、还款计划、还款记录
- **关键文件**:
  - `jinkoscf-spypc-factor/.../FactorLoanReceiptController.java`

### 2.9 ABS 资产池
- **功能描述**: 资产抽样分页、统计、资产池下拉
- **关键文件**:
  - `jinkoscf-spypc-abs/.../AbsAssetPoolController.java`

### 2.10 ABS 产品
- **功能描述**: 产品分页、新增/编辑、统计、债权方/债务人下拉
- **关键文件**:
  - `jinkoscf-spypc-abs/.../AbsProductController.java`

### 2.11 ABS 储架
- **功能描述**: 储架信息列表
- **关键文件**:
  - `jinkoscf-spypc-abs/.../AbsShelfController.java`

### 2.12 提前付款
- **功能描述**: 提前付款申请、签署合同、金额计算、发票占用校验
- **关键文件**:
  - `jinkoscf-spypc-transaction/.../prepay/PrepayFlowController.java`

### 2.13 融资交易
- **功能描述**: 融资申请、交易查询、资产统计、融资金额校验
- **关键文件**:
  - `jinkoscf-spypc-transaction/.../cash/TransactionCashTradeController.java`

### 2.14 转让
- **功能描述**: 转让发起、签收、资产列表、合同签署
- **关键文件**:
  - `jinkoscf-spypc-transaction/.../transferclient/TransferClientController.java`

### 2.15 支付
- **功能描述**: 到期/提前线下付款、支付分页、统计、导出
- **关键文件**:
  - `jinkoscf-spypc-transaction/.../pay/PayTradeController.java`

### 2.16 工作台
- **功能描述**: 待办、统计、节点操作
- **关键文件**:
  - `jinkoscf-spypc-business-common/.../workbench/DisplayController.java`

### 2.17 资产分析
- **功能描述**: 关联企业维护、资产池分析报告
- **关键文件**:
  - `jinkoscf-spypc-abs/.../toolbox/AssetPoolAnalyzeController.java`

## 3. 关键接口清单

| 接口路径 | 方法 | 功能说明 | 所属Controller |
|----------|------|----------|---------------|
| /limit/creditEfectLimitPage | POST | 已生效额度分页 | CustomLimitController |
| /limit/creditEfectLimitPageStatic | POST | 已生效额度统计 | CustomLimitController |
| /cust/company/getById | GET | 企业详情 | CustCompanyInfoController |
| /cust/company/saveOrUpdateCustCompany | POST | 保存/更新企业 | CustCompanyInfoController |
| /baseCustBuild/saveOrUpdateCustAllInfo | POST | 保存建档信息 | BaseCustBuildController |
| /baseCustBuild/licenseOcr | POST | 营业执照OCR | BaseCustBuildController |
| /baseCustBuild/startWorkflow | POST | 发起建档工作流 | BaseCustBuildController |
| /asset/importAsset | POST | 批量导入资产 | AssetController |
| /asset/saveAsset | POST | 新增应收账款 | AssetController |
| /invoice/insertAndVerify | POST | 发票新增并验真 | InvoiceController |
| /invoice/uploadAndOcr | POST | 上传并OCR | InvoiceController |
| /contract/queryContractInfo | POST | 查看合同 | ContractController |
| /factorAssetPc/listPageBuyer | POST | 买方资产分页 | FactorAssetController |
| /factorAssetPc/saveAsset | POST | 保存保理资产 | FactorAssetController |
| /factorCash/commitCash | POST | 提交保理融资 | FactorCashController |
| /factorCash/factorCashPage | POST | 融资列表分页 | FactorCashController |
| /loanReceipt/pageQuery | POST | 借据分页 | FactorLoanReceiptController |
| /assetPool/listPage | POST | 资产池分页 | AbsAssetPoolController |
| /product/listPage | POST | ABS产品分页 | AbsProductController |
| /ts/prepayFlow/prepayApply | POST | 提前付款申请 | PrepayFlowController |
| /ts/trade/cash/cashApply | POST | 融资申请 | TransactionCashTradeController |
| /transferClient/transferStartProcess | POST | 发起转让 | TransferClientController |
| /pay/listPayTradePage | POST | 支付分页 | PayTradeController |

## 4. 依赖关系

### 4.1 依赖的服务
| 服务/API | 用途 |
|----------|------|
| jinkoscf-business-common-api | 基础业务（客户、资产、合同、额度） |
| jinkoscf-transaction-api | 交易、放款、支付 |
| jinkoscf-factor-api | 保理资产、融资、借据 |
| jinkoscf-abs-api | ABS资产池、产品、储架 |
| jinkoscf-workflow-api | 工作流 |
| jinkoscf-gateway-api | 网关 |
| bccp-user/media/thirdparty/account/sign | BCCP基座组件 |
| lls-wkfl-api | 工作流引擎 |

### 4.2 被依赖的服务
本服务为前端入口，不被其他后端微服务依赖。

## 5. 数据模型

本服务主要通过 Dubbo 调用其他服务获取数据，自身不直接定义 DO 实体。主要使用的 DTO：

| DTO 类名 | 说明 |
|----------|------|
| AssetAddDTO / AssetImporterCommand | 资产新增/导入 |
| TransferStartProcessParamDTO / TransferSignInDTO | 转让发起/签收 |
| PrepayCollectDataVO / SelectedAssetVO | 提前付款数据 |
| FactorAssetStorageDTO / FactorAssetPageReq | 保理资产 |
| FactorLoanQueryReqDTO | 借据查询 |
| AbsAssetPoolPageReq / AbsProductPageParam | ABS分页 |
| LimitInfoReqDTO / CustLimitStaticDTO | 额度查询 |

## 6. 技术要点
- **分层架构**: Controller → Facade → Service，Dubbo 调用各业务域 API
- **安全机制**: @APIPermission、@DecryptRequest、@EncryptResponse、@SecureField
- **工作流集成**: lls-wkfl-api、jinkoscf-workflow-api 支持建档、融资等审批流程
- **Excel处理**: POI、EasyExcel 用于导入导出
- **第三方集成**: 发票验真、OCR、电子签章、实名认证
- **Knife4j**: Swagger API 文档
- **Nacos配置**: dataIds 为 acflow-spyPc、acflow-common
