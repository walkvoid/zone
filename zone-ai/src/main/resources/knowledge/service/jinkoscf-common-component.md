# jinkoscf-common-component 服务知识库

## 1. 服务概述
- **服务名称**: jinkoscf-common-component（公共业务组件服务，Demo应用名：acflow-business-common-web）
- **业务定位**: 公共业务组件，承载额度、客户、合同、产品、资产、申请等基础数据与配置管理能力
- **核心价值**: 作为底层业务组件，为 jinkoscf-transaction-component 和上层业务服务提供统一的额度信息、客户法人信息、合同信息、产品配置等基础能力

## 2. 核心功能模块

### 2.1 额度管理（LimitInfo）
- **功能描述**: 额度信息CRUD、申请额度、占用/释放、额度共享、额度类别、额度日志
- **关键文件**:
  - `jinkoscf-common-component-web/.../controller/LimitInfoController.java`
  - `jinkoscf-common-component-web/.../controller/ApplyLimitInfoController.java`
  - `jinkoscf-common-component-provider/.../LimitInfoOuterProviderImpl.java`
  - `jinkoscf-common-component-provider/.../ApplyLimitProviderImpl.java`

### 2.2 客户法人信息（CustLegalInfo）
- **功能描述**: 法人信息保存、查询、按客户ID获取、查询生效法人
- **关键文件**:
  - `jinkoscf-common-component-web/.../controller/CustLegalInfoController.java`
  - `jinkoscf-common-component-provider/.../CustLegalInfoOuterProviderImpl.java`

### 2.3 客户信息
- **功能描述**: 客户公司信息、客户账户、申请客户公司信息、申请客户账户信息
- **关键文件**:
  - `jinkoscf-common-component-web/.../controller/ApplyCustCompanyInfoController.java`
  - `jinkoscf-common-component-web/.../controller/CustAccountInfoController.java`
  - `jinkoscf-common-component-web/.../controller/ApplyCustAccountInfoController.java`
  - `jinkoscf-common-component-provider/.../CustInfoOuterProviderImpl.java`
  - `jinkoscf-common-component-provider/.../CustCompanyInfoOuterProviderImpl.java`
  - `jinkoscf-common-component-provider/.../CustAccountInfoOuterProviderImpl.java`

### 2.4 合同管理
- **功能描述**: 合同信息、合同模板、签约管理、合同交易关系
- **关键文件**:
  - `jinkoscf-common-component-web/.../controller/ContractInfoController.java`
  - `jinkoscf-common-component-provider/.../ContractInfoProviderImpl.java`
  - `jinkoscf-common-component-provider/.../ContractSignInfoProviderImpl.java`
  - `jinkoscf-common-component-provider/.../ContractTemplateFileProviderImpl.java`
  - `jinkoscf-common-component-provider/.../ContractTradeRelationProviderImpl.java`

### 2.5 产品管理
- **功能描述**: 产品信息、费率配置、资金配置、资产配置
- **关键文件**:
  - `jinkoscf-common-component-web/.../controller/ProductInfoController.java`
  - `jinkoscf-common-component-provider/.../ProductInfoProviderImpl.java`
  - `jinkoscf-common-component-provider/.../ProductRateConfigProviderImpl.java`
  - `jinkoscf-common-component-provider/.../ProductFeeConfigProviderImpl.java`

### 2.6 资产管理
- **功能描述**: 资产信息、应收账款、资产扩展字段、资产查询
- **关键文件**:
  - `jinkoscf-common-component-provider/.../AssetProviderImpl.java`
  - `jinkoscf-common-component-provider/.../AssetAccountsReceivableProviderImpl.java`
  - `jinkoscf-common-component-provider/.../AssetQueryProviderImpl.java`

## 3. 关键接口清单

| 接口路径 | 方法 | 功能说明 | 所属Controller |
|----------|------|----------|---------------|
| /limitInfo/* | POST/GET | 额度信息CRUD、分页 | LimitInfoController |
| /apply/limit/page | POST | 申请额度分页 | ApplyLimitInfoController |
| /apply/limit/saveApplyLimtInfo | POST | 新增授信信息 | ApplyLimitInfoController |
| /apply/limit/start | POST | 发起授信申请 | ApplyLimitInfoController |
| /apply/limit/saveAndStart | POST | 保存并发起授信 | ApplyLimitInfoController |
| /custLegalInfo/save | POST | 保存法人信息 | CustLegalInfoController |
| /custLegalInfo/getById | GET | 按ID获取法人 | CustLegalInfoController |
| /custLegalInfo/getByCustId | GET | 按客户ID获取法人 | CustLegalInfoController |
| /custLegalInfo/queryEffectLegal | POST | 查询生效法人 | CustLegalInfoController |
| /applyCustCompanyInfo/saveOrUpdate | POST | 保存/更新申请企业 | ApplyCustCompanyInfoController |
| /applyCustCompanyInfo/page | POST | 申请企业分页 | ApplyCustCompanyInfoController |
| /applyCustAccountInfo/* | POST | 申请客户账户管理 | ApplyCustAccountInfoController |
| /custAccountInfo/* | POST/GET | 客户账户管理 | CustAccountInfoController |
| /contractInfo/* | POST/GET | 合同信息管理 | ContractInfoController |
| /product/* | POST | 产品管理 | ProductInfoController |

## 4. 依赖关系

### 4.1 依赖的服务
| 服务/组件 | 用途 |
|----------|------|
| bccp-common-api/base/service | 公共基座 |
| bccp-user-api | 用户 |
| bccp-businessno-api | 业务编号 |
| bccp-media-api / bccp-attachment-api | 媒体/附件 |
| bccp-thirdparty-interfaces-api | 第三方接口 |
| lls-core / lls-redis / lls-freemarker | 框架 |
| jinkoscf-common-base | 公共基础 |

### 4.2 被依赖的服务
- jinkoscf-transaction-component（依赖额度、客户、合同、资产能力）
- jinkoscf-transaction（依赖额度、客户、合同接口）
- jinkoscf-business-common（依赖客户、额度接口）
- jinkoscf-workflow（依赖合同、客户接口）

## 5. 数据模型

| DO/DTO 类名 | 数据库表 | 说明 |
|-------------|---------|------|
| LimitInfoDO | limit_info | 额度信息 |
| ApplyLimitInfo | apply_limit_info | 申请额度信息 |
| CustLegalInfoDO | cust_legal_info | 客户法人信息 |
| CustAccountInfoDO | cust_account_info | 客户账户信息 |
| ApplyCustAccountInfoDO | apply_cust_account_info | 申请客户账户信息 |
| CustCompanyInfoDO | cust_company_info | 客户公司信息 |
| ApplyCustCompanyInfoDO | apply_cust_company_info | 申请客户公司信息 |
| ContractInfoDO | contract_info | 合同信息 |
| AssetInfoDO | asset_info | 资产信息 |
| ProductInfoDO | product_info | 产品信息 |

## 6. 技术要点
- **分层架构**: Controller → ApplicationService → DomainService → Dao
- **Dubbo Provider**: 提供 38+ 个 Dubbo 服务，覆盖额度、客户、合同、产品、资产等领域
- **MyBatis-Plus + XML Mapper**: 约 99 个 Mapper 文件
- **Aviator**: 表达式引擎用于业务规则计算
- **Freemarker**: 模板引擎用于合同等文档生成
- **BouncyCastle**: 加密库，用于安全相关处理
- **Nacos**: 配置中心，支持动态配置
- **hibernate-validator**: 参数校验
