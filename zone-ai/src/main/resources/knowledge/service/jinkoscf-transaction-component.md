# jinkoscf-transaction-component 服务知识库

## 1. 服务概述
- **服务名称**: jinkoscf-transaction-component（多级流转-融资报价组件，Demo应用名：acflow-trans-component-web）
- **业务定位**: 融资交易全流程组件，涵盖融资申请、放款、转让、结算、供应商准入及融资报价配置
- **核心价值**: 通过策略模式实现多银行融资申请的统一抽象，封装供应商准入、交易配置、资产管理等交易核心能力，为上层 jinkoscf-transaction 提供底层支撑

## 2. 核心功能模块

### 2.1 供应商准入（SupplierAccess）
- **功能描述**: 供应商准入管理、元数据维护、企业准入、招商银行白名单等
- **关键文件**:
  - `jinkoscf-transaction-component-web/.../controller/SupplierAccessController.java`
  - `jinkoscf-transaction-component-provider/.../SupplierAccessProviderImpl.java`
  - `jinkoscf-transaction-component-provider/.../SupplierAccessMetadataProviderImpl.java`
  - `jinkoscf-transaction-component-provider/.../EnterpriseAccessProviderImpl.java`

### 2.2 交易配置（TsTransactionConfig）
- **功能描述**: 交易配置管理、报价配置（CashBiddingConfig）、最低费率
- **关键文件**:
  - `jinkoscf-transaction-component-web/.../controller/TsTransactionConfigController.java`
  - `jinkoscf-transaction-component-provider/.../TsTransactionConfigProviderImpl.java`
  - `jinkoscf-transaction-component-provider/.../CashBiddingConfigProviderImpl.java`

### 2.3 融资申请（CashApply - 多银行实现）
- **功能描述**: 按资金方/银行实现融资申请，通过策略模式（FunderChannelCashApplyEnum）选择对应实现
- **关键文件**:
  - `jinkoscf-transaction-component-service/.../cashapply/BankofdlCashApplyServiceImpl.java`（大连银行）
  - `jinkoscf-transaction-component-service/.../cashapply/CgbCashApplyServiceImpl.java`（广发银行）
  - `jinkoscf-transaction-component-service/.../cashapply/BocomCashApplyServiceImpl.java`（交通银行）
  - `jinkoscf-transaction-component-service/.../cashapply/BobCashApplyServiceImpl.java`（北京银行）
  - `jinkoscf-transaction-component-service/.../cashapply/AbcCashApplyServiceImpl.java`（农业银行）
  - `jinkoscf-transaction-component-service/.../cashapply/PsbcCashApplyServiceImpl.java`（邮储银行）
  - `jinkoscf-transaction-component-service/.../cashapply/CmbchinaCashApplyServiceImpl.java`（招商银行）
  - `jinkoscf-transaction-component-service/.../cashapply/CibCashApplyServiceImpl.java`（兴业银行）
  - `jinkoscf-transaction-component-service/.../cashapply/CbhbCashApplyServiceImpl.java`（渤海银行）
  - `jinkoscf-transaction-component-service/.../cashapply/SpdCashApplyServiceImpl.java`（浦发银行）
  - `jinkoscf-transaction-component-service/.../cashapply/IcbcCashApplyServiceImpl.java`（工商银行）
  - `jinkoscf-transaction-component-service/.../cashapply/CmbcCashApplyServiceImpl.java`（民生银行）
  - `jinkoscf-transaction-component-service/.../cashapply/CiticCashApplyServiceImpl.java`（中信银行）

### 2.4 融资交易管理
- **功能描述**: 融资交易CRUD、放款（Issue）、转让（Transfer）、结算（Settle）、预付（Prepay）、重组（Refactor）
- **关键文件**:
  - `jinkoscf-transaction-component-web/.../controller/TsCashTradeController.java`
  - `jinkoscf-transaction-component-web/.../controller/TsCashTransactionController.java`
  - `jinkoscf-transaction-component-web/.../controller/TsTransferTransactionController.java`
  - `jinkoscf-transaction-component-provider/.../TsCashProviderImpl.java`
  - `jinkoscf-transaction-component-provider/.../TsCashTradeProviderImpl.java`
  - `jinkoscf-transaction-component-provider/.../TsIssueTransactionProviderImpl.java`
  - `jinkoscf-transaction-component-provider/.../TsTransferTransactionProviderImpl.java`
  - `jinkoscf-transaction-component-provider/.../TsSettleTransactionProviderImpl.java`

### 2.5 资产管理（TsAsset）
- **功能描述**: 交易资产、资产扩展、发票关联、资产查询
- **关键文件**:
  - `jinkoscf-transaction-component-web/.../controller/TsAssetController.java`
  - `jinkoscf-transaction-component-provider/.../TsAssetProviderImpl.java`
  - `jinkoscf-transaction-component-provider/.../TsAssetExtProviderImpl.java`
  - `jinkoscf-transaction-component-provider/.../TsInvoiceProviderImpl.java`

### 2.6 报价竞拍配置（CashBidding）
- **功能描述**: 报价竞拍CRUD、配置管理
- **关键文件**:
  - `jinkoscf-transaction-component-web/.../controller/CashBiddingController.java`
  - `jinkoscf-transaction-component-provider/.../CashBiddingOuterProviderImpl.java`
  - `jinkoscf-transaction-component-provider/.../CashBiddingConfigProviderImpl.java`

## 3. 关键接口清单

| 接口路径 | 方法 | 功能说明 | 所属Controller |
|----------|------|----------|---------------|
| /supplierAccess/page | POST | 供应商准入分页 | SupplierAccessController |
| /supplierAccess/commitAdmit | POST | 提交准入 | SupplierAccessController |
| /supplierAccess/save | POST | 保存准入 | SupplierAccessController |
| /supplierAccess/detail | POST | 准入详情 | SupplierAccessController |
| /supplierAccess/statistics | POST | 准入统计 | SupplierAccessController |
| /tsTransactionConfig/page | POST | 交易配置分页 | TsTransactionConfigController |
| /tsTransactionConfig/insert | POST | 新增交易配置 | TsTransactionConfigController |
| /tsTransactionConfig/updateById | POST | 更新交易配置 | TsTransactionConfigController |
| /tsTransactionConfig/minRate/add | POST | 新增最低费率 | TsTransactionConfigController |
| ts/cash/cashPreApply | POST | 融资预申请 | TsCashTradeController |
| ts/cash/cashApply | POST | 融资申请 | TsCashTradeController |
| ts/cash/cashApplyCancel | POST | 取消融资申请 | TsCashTradeController |
| ts/cash/cashSign | POST | 融资签收 | TsCashTradeController |
| /cashBidding/insert | POST | 新增报价 | CashBiddingController |
| /cashBidding/pageCashBidding | POST | 报价分页 | CashBiddingController |
| /tsAsset/page | POST | 资产分页 | TsAssetController |
| /tsAsset/detail | POST | 资产详情 | TsAssetController |
| /tsTransferTransaction/transferApply | POST | 转让申请 | TsTransferTransactionController |
| /tsTransferTransaction/transferSignIn | POST | 转让签收 | TsTransferTransactionController |
| /enterpriseAccess/page | POST | 企业准入分页 | EnterpriseAccessController |

## 4. 依赖关系

### 4.1 依赖的服务
| 服务/组件 | 用途 |
|----------|------|
| jinkoscf-common-component-api | 额度、客户、合同、资产基础能力 |
| jinkoscf-transaction-api | 交易接口 |
| jinkoscf-workflow-api | 工作流 |
| jinkoscf-gateway-api | 网关银行对接 |
| bankofdl-sdk-api | 大连银行SDK |
| cbhb-sdk-api | 渤海银行SDK |
| bccp-thirdparty-interfaces-api | 第三方接口 |
| bccp-reqchain-api | 请求链路 |
| bccp-businessno-api | 业务编号 |
| lls-core / lls-redis | 框架/Redis |

### 4.2 被依赖的服务
- jinkoscf-transaction（调用融资申请、供应商准入、交易配置等接口）
- jinkoscf-batch（调用交易状态查询接口）
- jinkoscf-workflow（调用交易资产接口）

## 5. 数据模型

| DO/DTO 类名 | 数据库表 | 说明 |
|-------------|---------|------|
| SupplierAccessDO | supplier_access | 供应商准入 |
| SupplierAccessMetadataDO | supplier_access_metadata | 准入元数据 |
| SupplierAccessExtDO | supplier_access_ext | 准入扩展 |
| TsTransactionConfigDO | ts_transaction_config | 交易配置 |
| TsTransactionDO | ts_transaction | 交易主表 |
| TsCashTransactionDO | ts_cash_transaction | 融资交易 |
| TsCashTransactionExtDO | ts_cash_transaction_ext | 融资交易扩展 |
| TsAssetDO | ts_asset | 交易资产 |
| TsAssetExtDO | ts_asset_ext | 资产扩展 |
| TsInvoiceDO | ts_invoice | 发票 |
| CashBiddingServiceDO | cash_bidding | 融资报价 |
| CashBiddingConfigDO | cash_bidding_config | 报价配置 |
| EnterpriseAccessDO | enterprise_access | 企业准入 |
| PayTradeDO | pay_trade | 支付交易 |

## 6. 技术要点
- **CashApply 策略模式**: FunderChannelCashApplyEnum 按资金渠道选择不同的 CashApplyService 实现，覆盖 13+ 家银行
- **融资全流程**: 预申请 → 申请 → 签收 → 放款 → 转让 → 结算，每个环节独立 Provider
- **多银行准入**: 按银行实现不同的准入应用服务（Abc、Bocom、Bob、Cgb、Psbc等）
- **与 common-component 关系**: 依赖 jinkoscf-common-component-api 的额度、客户、合同、资产基础能力
- **Dubbo Provider**: 提供 30+ 个 Dubbo 服务
- **约 28 个 Mapper XML**: 覆盖供应商准入、交易、融资、资产等数据访问
- **银行SDK集成**: 直接集成 bankofdl-sdk-api、cbhb-sdk-api 等银行SDK
