# jinkoscf-tools 服务知识库

## 1. 服务概述
- **服务名称**: jinkoscf-tools（应用名：acflow-tools-web）
- **业务定位**: 工具技术类服务，提供用户管理、角色权限、组织机构、附件管理、媒体操作、雪花ID、业务号映射等通用基础能力
- **核心价值**: 作为平台基础能力层，统一管理用户体系、权限体系、组织架构，提供ID生成、附件存储、媒体操作等工具能力

## 2. 核心功能模块

### 2.1 用户管理（BaseUserInfo / PcUserInfo）
- **功能描述**: 客户管理员管理、PC端用户增删改查、冻结/解冻、企业用户管理、晶科SSO用户同步
- **关键文件**:
  - `jinkoscf-tools-web/.../controller/BaseUserInfoController.java`
  - `jinkoscf-tools-web/.../controller/PcUserInfoController.java`
  - `jinkoscf-tools-web/.../controller/AcFlowUserInfoController.java`
  - `jinkoscf-tools-web/.../controller/JinkoUserController.java`
  - `jinkoscf-tools-web/.../controller/JinkoLoginController.java`

### 2.2 角色权限（PcUserCustRole）
- **功能描述**: 审批通过批量分配角色、新增用户并分配角色、角色查询
- **关键文件**:
  - `jinkoscf-tools-web/.../controller/PcUserCustRoleController.java`
  - `jinkoscf-tools-web/.../controller/BaseUserCustRoleController.java`

### 2.3 附件管理（Attachment）
- **功能描述**: 隐私政策获取、多附件上传、下载URL
- **关键文件**:
  - `jinkoscf-tools-web/.../controller/AttachmentController.java`
  - `jinkoscf-tools-service/.../AcflowAttachmentService.java`

### 2.4 媒体操作（AcflowMediaOpera）
- **功能描述**: 法人身份证同步到客户管理员、模板文件上传
- **关键文件**:
  - `jinkoscf-tools-web/.../controller/AcflowMediaOperaController.java`

### 2.5 雪花ID（SnowFlake）
- **功能描述**: 单ID生成、批量ID生成、交易编号生成，支持 Hutool Snowflake 和百度 UID 两种算法
- **关键文件**:
  - `jinkoscf-tools-web/.../controller/SnowFlakeController.java`
  - `jinkoscf-tools-provider/.../BaiduUidProviderImpl.java`

### 2.6 组织机构
- **功能描述**: 组织树管理、导入导出、IAM组织同步、用户组织关系
- **关键文件**:
  - `jinkoscf-tools-web/.../controller/AcflowOrgController.java`
  - `jinkoscf-tools-web/.../controller/IamOrgController.java`
  - `jinkoscf-tools-web/.../controller/IamUserOrgController.java`
  - `jinkoscf-tools-web/.../controller/PcAuthOrgController.java`

### 2.7 产品配置
- **功能描述**: 产品开通、保理开通、订单融资
- **关键文件**:
  - `jinkoscf-tools-web/.../controller/PcAuthProductConfigController.java`

### 2.8 其他工具
- **功能描述**: 供应商分类、银行信息、菜单资源、静态枚举
- **关键文件**:
  - `jinkoscf-tools-web/.../controller/SupplyCategoryController.java`
  - `jinkoscf-tools-web/.../controller/AcflowBankInfoController.java`
  - `jinkoscf-tools-web/.../controller/BaseAuthResourceController.java`
  - `jinkoscf-tools-web/.../controller/StaticEnumsController.java`

## 3. 关键接口清单

| 接口路径 | 方法 | 功能说明 | 所属Controller |
|----------|------|----------|---------------|
| /baseUserInfo/adminPage | POST | 分页查询客户管理员 | BaseUserInfoController |
| /baseUserInfo/detailById | POST | 客户管理员详情 | BaseUserInfoController |
| /baseUserInfo/getManagerList | POST | 获取客户经理 | BaseUserInfoController |
| /userInfo/pc/saveOrUpdate | POST | 新增/修改用户 | PcUserInfoController |
| /userInfo/pc/saveUserAndRole | POST | 新增/修改用户和角色 | PcUserInfoController |
| /userInfo/pc/updateUserStatus | POST | 更新用户状态 | PcUserInfoController |
| /userInfo/pc/freeze | POST | 冻结用户 | PcUserInfoController |
| /userInfo/pc/unfreeze | POST | 解冻用户 | PcUserInfoController |
| /userInfo/pc/getUserMenuPermList | POST | 用户菜单权限列表 | PcUserInfoController |
| /userCustRoleRel/pc/saveBatchRole | POST | 审批通过并批量分配角色 | PcUserCustRoleController |
| /userCustRoleRel/pc/saveOrUpdateAccountAndUserInfo | POST | 新增用户并分配角色 | PcUserCustRoleController |
| /acflow/attachment/uploadMultiple | POST | 批量上传附件 | AttachmentController |
| /acflow/attachment/downloadUrl | POST | 获取下载URL | AttachmentController |
| /media/opera/do/copyMediaFileToAuth | POST | 法人身份证同步 | AcflowMediaOperaController |
| /snowFlake/generateTransactionId | GET | 批量生成交易编号 | SnowFlakeController |
| /snowFlake/getSnowFlakeId | GET/POST | 单个雪花ID | SnowFlakeController |
| /api/jinko/callback | POST | SSO回调 | JinkoLoginController |
| /org/* | POST/GET | 组织树管理 | AcflowOrgController |

## 4. 依赖关系

### 4.1 依赖的服务
| 服务/接口 | 用途 |
|----------|------|
| jinkoscf-business-common-api | 业务公共 |
| jinkoscf-workflow-api | 工作流 |
| jinkoscf-gateway-api | 网关（JinkoClientProvider） |
| jinkoscf-transaction-api | 交易 |
| jinkoscf-factor-api | 保理 |
| bccp-notice/media/auth/user/attachment/bank | BCCP基座组件 |

### 4.2 被依赖的服务
- jinkoscf-batch（调用 IamUserOrg、IamOrg 等接口）
- jinkoscf-business-common（调用用户、角色接口）
- jinkoscf-transaction（调用用户接口）

## 5. 数据模型

| DO/DTO 类名 | 数据库表 | 说明 |
|-------------|---------|------|
| SupplyCategoryDO | supply_category | 供应商分类 |
| IamUserOrgDO | iam_user_org | IAM用户组织关系 |
| IamOrgDO | iam_org | IAM组织 |
| BusinessNoMappingDO | business_no_mapping | 业务号映射 |
| LinkInfoDO | link_info | 链接信息 |
| JkUserInfoDO | user_info_jk | 晶科用户 |

## 6. 技术要点
- **雪花ID双实现**: 本地 Hutool Snowflake + Dubbo 调用 BaiduUidProvider（百度UID算法）
- **晶科SSO集成**: JinkoSSoDomainService 实现 SSO 登录/登出/授权
- **用户同步**: JinkoUserSyncDomainService 按工号/组织同步晶科用户
- **安全机制**: Shiro + @EncryptResponse / @DecryptRequest / @SecureField
- **Nacos配置**: dataIds 为 jinkoscf-tools、jinkoscf-common
- **Dubbo Provider**: 提供 15+ 个 Dubbo 服务（用户、角色、组织、雪花ID、附件等）
