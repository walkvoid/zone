# jinkoscf-transaction 服务知识库

## 1. 服务概述
- **服务名称**: jinkoscf-transaction（应用名：acflow-transaction-web）
- **业务定位**: 供应链金融交易中心服务，覆盖融资放款、付款兑付、资产交易、平台报表、分润开票、报价竞拍等核心交易流程
- **核心价值**: 作为交易核心引擎，统一管理融资放款全流程、多渠道付款兑付、资产交易审批、对账结算，以及报价竞拍和分润开票等增值业务

## 2. 核心功能模块

### 2.1 融资放款（Cash）
- **功能描述**: 融资申请、放款管理、中登登记、资金方对接、多银行融资校验
- **关键文件**:
  - `jinkoscf-transaction-web/.../controller/CashController.java`
  - `jinkoscf-transaction-provider/.../CashProvider.java`
  - `jinkoscf-transaction-service/.../cash/CashDomainService.java`
  - `jinkoscf-transaction-service/.../checker/BankofdlCashChecker.java`
  - `jinkoscf-transaction-service/.../checker/AbcCashChecker.java`
  - `jinkoscf-transaction-service/.../checker/BobCashChecker.java`
  - `jinkoscf-transaction-service/.../checker/CgbCashChecker.java`
  - `jinkoscf-transaction-service/.../checker/PsbcCashChecker.java`
  - `jinkoscf-transaction-service/.../checker/CashCheckerDomainService.java`

### 2.2 付款兑付（PayTrade）
- **功能描述**: 兑付交易、四库付款、渠道选择、告警统计
- **关键文件**:
  - `jinkoscf-transaction-web/.../controller/PayTradeController.java`
  - `jinkoscf-transaction-provider/.../PayTradeProviderImpl.java`
  - `jinkoscf-transaction-service/.../paytrade/PayTradeServiceImpl.java`
  - `jinkoscf-transaction-service/.../paytrade/PayTradeChannelSelector.java`
  - `jinkoscf-transaction-service/.../paytrade/PayTradeCheckerFactory.java`

### 2.3 资产交易（Asset / TsAsset）
- **功能描述**: 资产审批、过户、持有、流转、销毁申请、挂牌统计
- **关键文件**:
  - `jinkoscf-transaction-web/.../controller/AssetController.java`
  - `jinkoscf-transaction-web/.../controller/TsAssetController.java`
  - `jinkoscf-transaction-provider/.../TsAssetBaseProviderImpl.java`
  - `jinkoscf-transaction-provider/.../AgwAssetProviderImpl.java`

### 2.4 平台报表（PlatReport）
- **功能描述**: 汇缴/日汇统计、报表生成与导出
- **关键文件**:
  - `jinkoscf-transaction-web/.../controller/PlatReportController.java`
  - `jinkoscf-transaction-provider/.../PlatReportProviderImpl.java`
  - `jinkoscf-transaction-service/.../platreport/PlatReportHjListService.java`

### 2.5 分润开票（FeeAllocate / PlatFeeOpenBill）
- **功能描述**: 费用分配、开票、平台开票管理、分润同步计算
- **关键文件**:
  - `jinkoscf-transaction-web/.../controller/FeeAllocateController.java`
  - `jinkoscf-transaction-web/.../controller/PlatFeeOpenBillController.java`
  - `jinkoscf-transaction-service/.../feeallocate/TsFeeAllocateServiceImpl.java`

### 2.6 报价竞拍（CashBidding）
- **功能描述**: 报价创建、导入、竞拍、费用计算
- **关键文件**:
  - `jinkoscf-transaction-web/.../controller/CashBiddingController.java`
  - `jinkoscf-transaction-provider/.../CashBiddingProvider.java`
  - `jinkoscf-transaction-service/.../cashbidding/CashBiddingInfoServiceImpl.java`

### 2.7 提前清偿（Prepay）
- **功能描述**: 提前清偿审核、线下清偿、四库重试
- **关键文件**:
  - `jinkoscf-transaction-web/.../controller/TsPrepayController.java`
  - `jinkoscf-transaction-provider/.../PrepayProvider.java`

### 2.8 对账（Reconciliation）
- **功能描述**: 交易对账、对账明细与汇总
- **关键文件**:
  - `jinkoscf-transaction-web/.../controller/ReconciliationSummaryController.java`
  - `jinkoscf-transaction-web/.../controller/ReconciliationDetailController.java`

## 3. 关键接口清单

| 接口路径 | 方法 | 功能说明 | 所属Controller |
|----------|------|----------|---------------|
| /tsCash/calculateLoanInfo | GET | 计算放款信息 | CashController |
| /tsCash/commitZhongdengRegister | POST | 提交中登登记 | CashController |
| /tsCash/queryTsTransaction | POST | 查询交易 | CashController |
| /tsCash/getAllCashConfig | POST | 获取放款配置 | CashController |
| /tsCash/queryTrasactionManagerPage | POST | 交易管理分页 | CashController |
| /tsCash/updateLoanDate | POST | 更新放款日期 | CashController |
| /pay/listPayTradePage | POST | 兑付分页 | PayTradeController |
| /pay/offlineDuePay | POST | 线下到期兑付 | PayTradeController |
| /pay/manualSikuPay | GET | 手动四库付款 | PayTradeController |
| /pay/payTradeAlarmStatistic | POST | 兑付告警统计 | PayTradeController |
| /asset/submitApproving | POST | 提交审批 | AssetController |
| /asset/abolishCertificate | POST | 作废凭证 | AssetController |
| /tsAsset/cashTsAssetPage | POST | 融资资产分页 | TsAssetController |
| /tsAsset/tsAssetTradeStatistics | POST | 资产交易统计 | TsAssetController |
| /tsAsset/currPublishedPage | POST | 当前挂牌分页 | TsAssetController |
| /platReport/generatePlatReportHjList | POST | 生成汇缴列表 | PlatReportController |
| /platReport/pagePlatReportHjList | POST | 汇缴列表分页 | PlatReportController |
| /feeAllocate/feeAllocatePage | POST | 分润分页 | FeeAllocateController |
| /feeAllocate/openBill | POST | 开票 | FeeAllocateController |
| /feeAllocate/feeAllocateCalc | POST | 分润计算 | FeeAllocateController |
| /platFeeOpenBill/platFeeOpenBillPage | POST | 平台开票分页 | PlatFeeOpenBillController |
| /platFeeOpenBill/platFeeManualOpenBill | POST | 手动开票 | PlatFeeOpenBillController |
| /cashBidding/createCashBidding | POST | 创建报价 | CashBiddingController |
| /cashBidding/startCashBidding | POST | 发起竞拍 | CashBiddingController |
| /cashBidding/calculateCashFee | POST | 计算放款费用 | CashBiddingController |

## 4. 依赖关系

### 4.1 依赖的服务
| 服务/接口 | 用途 |
|----------|------|
| jinkoscf-transaction-component | 交易组件（融资申请、供应商准入、交易配置） |
| jinkoscf-common-component | 公共组件（额度、客户、合同） |
| jinkoscf-business-common-api | 资产、发票、合同、客户、产品 |
| jinkoscf-gateway-api | 银行对接 Provider |
| jinkoscf-workflow-api | 工作流 |
| bccp-apply-loan | 融资申请基座 |
| lls-wkfl-api | 工作流引擎 |

### 4.2 被依赖的服务
- jinkoscf-batch（调用交易、兑付接口）
- jinkoscf-workflow（调用交易接口）
- jinkoscf-spypc（调用交易、兑付接口）

## 5. 数据模型

| DO/DTO 类名 | 数据库表 | 说明 |
|-------------|---------|------|
| PlatReportHjListDO | plat_report_hj_list | 汇缴列表 |
| PlatReportHjStatisticsDO | plat_report_hj_statistics | 汇缴统计 |
| PlatReportRhStatisticsDO | plat_report_rh_statistics | 日汇统计 |
| ReconciliationDetailDO | reconciliation_detail | 对账明细 |
| ReconciliationSummaryDO | reconciliation_summary | 对账汇总 |
| ReconciliationTransLogDO | reconciliation_trans_log | 对账交易日志 |
| PayTradeAccountInfoDO | pay_trade_account_info | 兑付账户信息 |
| BankCashRetDO | bank_cash_ret | 银行融资结果 |
| TsBusinessConfDO | ts_business_conf | 交易业务配置 |
| TsAssetFunderRelaDO | ts_asset_funder_rela | 资产资金方关系 |
| TsAssetAbolishApplyDO | ts_asset_abolish_apply | 资产作废申请 |
| TransferAssetTaskDO | transfer_asset_task | 转让资产任务 |
| OperationPlatformLedgerDO | operation_platform_ledger | 运营平台台账 |
| TsLimitSikuInfoDO | ts_limit_siku_info | 四库限额信息 |
| TsElectronicArchivesInfoDO | ts_electronic_archives_info | 电子档案信息 |
| FacFinanceInfoDO | fac_finance_info | 保理融资信息 |

## 6. 技术要点
- **多资金方 Checker**: 通过 FunderChannelCodeEnum 和 AbstractFunderCashChecker 抽象不同银行的融资校验逻辑
- **兑付渠道选择**: PayTradeChannelSelector 监听 PayTradeEvent，按资金方选择 IPayTradeChannelHandler 处理兑付
- **对账策略**: ReconciliationStrategyFactory 按资金方注册不同对账策略
- **合同适配**: 多种 Adapter（AbcFactoringAgreementAdapter、BobCreditAuthorizationAdapter 等）适配不同资金方合同模板
- **MyBatis-Plus**: 使用 @TableName、BaseService，支持 autoResultMap
- **工作流集成**: 依赖 lls-wkfl-api，融资审批等流程与工作流集成
