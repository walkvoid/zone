# jinkoscf-business-common 服务知识库

## 1. 服务概述
- **服务名称**: jinkoscf-business-common（公共业务服务）
- **业务定位**: 保理业务公共微服务，承载额度、客户、资产、发票、合同、产品参数、保理还款等核心业务能力
- **核心价值**: 作为业务中台，为其他微服务提供统一的额度管理、客户管理、资产发票合同管理、保理还款等基础业务能力，通过 HTTP 和 Dubbo 双通道对外输出

## 2. 核心功能模块

### 2.1 额度管理（FactorLimit / CustomLimit）
- **功能描述**: 融资额度、开单额度、保理额度管理，支持授信申请、额度变更、冻结/解冻
- **关键文件**:
  - `jinkoscf-business-common-web/.../limit/web/controller/FactorLimitController.java`
  - `jinkoscf-business-common-web/.../limit/web/controller/CustomLimitController.java`
  - `jinkoscf-business-common-provider/.../CustomLimitProviderImpl.java`
  - `jinkoscf-business-common-provider/.../CustomApplyLimitProviderImpl.java`

### 2.2 客户管理（FlowCust / CustCompanyInfo / CustAuthor）
- **功能描述**: 客户信息、企业信息、客户管理员、联系人管理
- **关键文件**:
  - `jinkoscf-business-common-web/.../cust/web/controller/FlowCustController.java`
  - `jinkoscf-business-common-web/.../cust/web/controller/CustCompanyInfoController.java`
  - `jinkoscf-business-common-web/.../cust/web/controller/CustAuthorController.java`
  - `jinkoscf-business-common-provider/.../FlowCustCompanyInfoProviderImpl.java`

### 2.3 资产管理（AcflowAsset）
- **功能描述**: 资产分页查询、导入、提交审批、转让描述
- **关键文件**:
  - `jinkoscf-business-common-web/.../asset/web/controller/AcflowAssetController.java`
  - `jinkoscf-business-common-provider/.../AssetInfoProviderImpl.java`

### 2.4 发票管理（Invoice）
- **功能描述**: 发票增删改查、OCR识别、验真、业务绑定、批量操作
- **关键文件**:
  - `jinkoscf-business-common-web/.../asset/web/controller/InvoiceController.java`
  - `jinkoscf-business-common-provider/.../InvoiceProviderImpl.java`

### 2.5 合同管理（ContractTrade / ContractBusiness）
- **功能描述**: 贸易合同管理、合同签署、发票关联、合同影像
- **关键文件**:
  - `jinkoscf-business-common-web/.../asset/web/controller/ContractTradeController.java`
  - `jinkoscf-business-common-provider/.../ContractProviderImpl.java`

### 2.6 产品参数（ParamBusiness / ConfigManage）
- **功能描述**: 流程配置、业务参数配置、客户自定义配置
- **关键文件**:
  - `jinkoscf-business-common-web/.../product/web/controller/ParamBusinessController.java`
  - `jinkoscf-business-common-web/.../product/web/controller/ConfigManageController.java`

### 2.7 保理还款（FactorRepayment / FactorPaySchedule / FactorLoanReceipt）
- **功能描述**: 还款记录、还款试算、还款执行、还款计划生成、借据管理
- **关键文件**:
  - `jinkoscf-business-common-web/.../factor/web/controller/FactorRepaymentController.java`
  - `jinkoscf-business-common-web/.../factor/web/controller/FactorPayScheduleController.java`
  - `jinkoscf-business-common-web/.../factor/web/controller/FactorLoanReceiptController.java`

### 2.8 快照管理
- **功能描述**: 发票/合同变动快照记录与差异计算
- **关键文件**:
  - `jinkoscf-business-common-web/.../snapshot/web/GenericSnapshotController.java`

## 3. 关键接口清单

| 接口路径 | 方法 | 功能说明 | 所属Controller |
|----------|------|----------|---------------|
| /factor/limit/saveAndCommit | POST | 新增授信保存提交 | FactorLimitController |
| /factor/limit/creditEfectLimitPage | POST | 融资额度已生效分页 | FactorLimitController |
| /factor/limit/doFreezeLoanCreditLimit | POST | 冻结融资额度 | FactorLimitController |
| /factor/limit/doUnfreezeCreditLimit | POST | 解冻额度 | FactorLimitController |
| /limit/saveApplyLimitInfo | POST | 新增授信信息 | CustomLimitController |
| /acflowCustInfo/* | POST/GET | 客户信息CRUD | FlowCustController |
| /custCompanyInfo/saveOrUpdateCustInfo | POST | 新增或更新企业信息 | CustCompanyInfoController |
| /custAuthorInfo/page | POST | 分页查询客户管理员 | CustAuthorController |
| /asset/pageInfo | POST | 资产分页查询 | AcflowAssetController |
| /invoice/insertAndVerify | POST | 发票新增并验真 | InvoiceController |
| /invoice/uploadAndOcr | POST | 上传-OCR-验真入库 | InvoiceController |
| /invoice/band | POST | 业务绑定发票 | InvoiceController |
| /contractTrade/saveContract | POST | 保存贸易合同 | ContractTradeController |
| /contractTrade/pageContract | POST | 贸易合同分页 | ContractTradeController |
| /paramBusiness/list | POST | 业务配置列表 | ParamBusinessController |
| /config-manage/createOrSave | POST | 客户自定义配置保存 | ConfigManageController |
| /factor/repayment/repay | POST | 还款执行 | FactorRepaymentController |
| /factor/repayment/repayTrial | POST | 还款试算 | FactorRepaymentController |
| /factor/paySchedule/generate | POST | 生成还款计划 | FactorPayScheduleController |
| /factor/loanReceipt/pageQuery | POST | 借据分页 | FactorLoanReceiptController |
| /snapshot/computeSnapshotDiff | POST | 变动差异计算 | GenericSnapshotController |

## 4. 依赖关系

### 4.1 依赖的服务
| 服务/接口 | 用途 |
|----------|------|
| bccp-account | 借据、还款、还款计划、账户 |
| bccp-common-service | 通用能力 |
| AcFlowUserCustProvider | 用户客户关系 |
| TsAssetProvider / TsTransactionProvider | 交易资产与交易 |
| WorkflowNewProvider | 工作流引擎 |
| IDisplayProvider | 工作台展示 |
| SupplierAccessProvider | 供应商接入 |
| IMediaOperaProvider | 媒体操作 |
| BusinessNoProvider | 业务编号 |

### 4.2 被依赖的服务
- jinkoscf-batch（调用额度、客户、发票、合同等接口）
- jinkoscf-transaction（调用资产、发票、客户接口）
- jinkoscf-workflow（调用客户、合同接口）
- jinkoscf-spypc（调用全部公共业务接口）
- jinkoscf-gateway（调用资产、客户接口）

## 5. 数据模型

| DO/DTO 类名 | 数据库表 | 说明 |
|-------------|---------|------|
| Invoice | invoice | 发票 |
| InvoiceBusinessRel | invoice_business_rel | 业务与发票关联 |
| InvoiceContractRel | invoice_contract_rel | 合同与发票关联 |
| CustomLimitInfoDO | limit_info | 额度信息 |
| CustApplyLimitInfo | apply_limit_info | 授信申请 |
| ApplyLimitVerifyInfoDO | apply_limit_verify_info | 授信认证信息 |
| ParamBusiness | param_business | 业务参数 |
| ConfigManage | config_manage | 配置管理 |
| GenericSnapshotRecordDO | generic_snapshot_record | 通用快照记录 |
| BankPriceQuotationInfoDO | bank_price_quotation_info | 银行报价 |
| SettleDayConfigDO | settle_day_config | 结算日配置 |
| ContractSignSubjectDO | contract_sign_subject | 合同签署方 |
| ContractSignRetryDO | contract_sign_retry | 合同签署重试 |
| CustGuarantor | cust_guarantor | 保证人 |
| ParamFunderConfigOuterDO | param_funder_config | 资金方配置 |
| ParamFunderContractDO | param_funder_contract | 资金方合同参数 |

## 6. 技术要点
- **Dubbo 服务**: 提供 24+ 个 Dubbo 服务，覆盖额度、客户、资产、发票、合同、产品、还款等领域
- **MyBatis-Plus**: 使用 @TableName 映射实体与表，支持扩展 resultMap
- **Swagger**: Controller 使用 @Api、@ApiOperation 文档化接口
- **权限控制**: Shiro + @APIPermission 接口权限
- **数据安全**: @EncryptResponse / @DecryptRequest 敏感数据加解密
- **Excel**: EasyExcel / ExcelUtil 用于导入导出
- **工作流集成**: 与 lls-wkfl-api 集成，用于审批流程
- **文档解析**: 多银行保理文档解析（IDocParser 实现）
- **Nacos 动态配置**: 使用 @NacosValue 读取动态配置
