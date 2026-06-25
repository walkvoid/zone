-- ============================================
-- 金融模块建表：A股股票数据
-- ============================================

-- ============================================
-- 1. 股票基本信息表 (stock_info)
-- ============================================
CREATE TABLE IF NOT EXISTS `stock_info` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `stock_code`        VARCHAR(16)  NOT NULL                COMMENT '股票代码，如 SH.600001',
    `stock_name`        VARCHAR(32)  NOT NULL                COMMENT '股票名称',
    `market`            VARCHAR(8)   NOT NULL                COMMENT '市场：SH-上海, SZ-深圳, BJ-北京',
    `listing_date`      DATE         DEFAULT NULL            COMMENT '上市日期',
    `total_shares`      BIGINT       DEFAULT NULL            COMMENT '总股本（股）',
    `circulating_shares` BIGINT      DEFAULT NULL            COMMENT '流通股本（股）',
    `industry_code`     VARCHAR(32)  DEFAULT NULL            COMMENT '行业编码',
    `status`            TINYINT      DEFAULT 1               COMMENT '状态：1-上市, 0-退市, 2-ST',
    `create_id`         BIGINT       DEFAULT NULL            COMMENT '创建者ID',
    `create_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`         BIGINT       DEFAULT NULL            COMMENT '更新者ID',
    `update_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stock_code` (`stock_code`),
    KEY `idx_market` (`market`),
    KEY `idx_industry_code` (`industry_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股票基本信息表';


-- ============================================
-- 2. 股票日交易数据表 (stock_daily)
-- 数据量大，不加审计字段
-- ============================================
CREATE TABLE IF NOT EXISTS `stock_daily` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `stock_code`        VARCHAR(16)  NOT NULL                COMMENT '股票代码',
    `trade_date`        DATE         NOT NULL                COMMENT '交易日',
    `open`              DECIMAL(12,3) DEFAULT NULL           COMMENT '开盘价',
    `high`              DECIMAL(12,3) DEFAULT NULL           COMMENT '最高价',
    `low`               DECIMAL(12,3) DEFAULT NULL           COMMENT '最低价',
    `close`             DECIMAL(12,3) DEFAULT NULL           COMMENT '收盘价',
    `pre_close`         DECIMAL(12,3) DEFAULT NULL           COMMENT '前收盘价',
    `volume`            BIGINT       DEFAULT NULL            COMMENT '成交量（股）',
    `amount`            DECIMAL(18,2) DEFAULT NULL           COMMENT '成交额（元）',
    `change_pct`        DECIMAL(10,4) DEFAULT NULL           COMMENT '涨跌幅（%）',
    `turnover_rate`     DECIMAL(10,4) DEFAULT NULL           COMMENT '换手率（%）',
    `amplitude`         DECIMAL(10,4) DEFAULT NULL           COMMENT '振幅（%）',
    `create_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stock_trade_date` (`stock_code`, `trade_date`),
    KEY `idx_trade_date` (`trade_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股票日交易数据表';


-- ============================================
-- 3. 股票周交易数据表 (stock_weekly)
-- ============================================
CREATE TABLE IF NOT EXISTS `stock_weekly` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `stock_code`        VARCHAR(16)  NOT NULL                COMMENT '股票代码',
    `trade_date`        DATE         NOT NULL                COMMENT '该周最后一个交易日',
    `open`              DECIMAL(12,3) DEFAULT NULL           COMMENT '周开盘价',
    `high`              DECIMAL(12,3) DEFAULT NULL           COMMENT '周最高价',
    `low`               DECIMAL(12,3) DEFAULT NULL           COMMENT '周最低价',
    `close`             DECIMAL(12,3) DEFAULT NULL           COMMENT '周收盘价',
    `volume`            BIGINT       DEFAULT NULL            COMMENT '周成交量（股）',
    `amount`            DECIMAL(18,2) DEFAULT NULL           COMMENT '周成交额（元）',
    `change_pct`        DECIMAL(10,4) DEFAULT NULL           COMMENT '周涨跌幅（%）',
    `turnover_rate`     DECIMAL(10,4) DEFAULT NULL           COMMENT '周换手率（%）',
    `amplitude`         DECIMAL(10,4) DEFAULT NULL           COMMENT '周振幅（%）',
    `create_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stock_trade_date` (`stock_code`, `trade_date`),
    KEY `idx_trade_date` (`trade_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股票周交易数据表';


-- ============================================
-- 4. 股票月交易数据表 (stock_monthly)
-- ============================================
CREATE TABLE IF NOT EXISTS `stock_monthly` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `stock_code`        VARCHAR(16)  NOT NULL                COMMENT '股票代码',
    `trade_date`        DATE         NOT NULL                COMMENT '该月最后一个交易日',
    `open`              DECIMAL(12,3) DEFAULT NULL           COMMENT '月开盘价',
    `high`              DECIMAL(12,3) DEFAULT NULL           COMMENT '月最高价',
    `low`               DECIMAL(12,3) DEFAULT NULL           COMMENT '月最低价',
    `close`             DECIMAL(12,3) DEFAULT NULL           COMMENT '月收盘价',
    `volume`            BIGINT       DEFAULT NULL            COMMENT '月成交量（股）',
    `amount`            DECIMAL(18,2) DEFAULT NULL           COMMENT '月成交额（元）',
    `change_pct`        DECIMAL(10,4) DEFAULT NULL           COMMENT '月涨跌幅（%）',
    `turnover_rate`     DECIMAL(10,4) DEFAULT NULL           COMMENT '月换手率（%）',
    `amplitude`         DECIMAL(10,4) DEFAULT NULL           COMMENT '月振幅（%）',
    `create_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stock_trade_date` (`stock_code`, `trade_date`),
    KEY `idx_trade_date` (`trade_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股票月交易数据表';


-- ============================================
-- 5. 股票分时数据表 (stock_intraday)
-- 数据量巨大，仅保留 id
-- ============================================
CREATE TABLE IF NOT EXISTS `stock_intraday` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `stock_code`        VARCHAR(16)  NOT NULL                COMMENT '股票代码',
    `trade_date`        DATE         NOT NULL                COMMENT '交易日',
    `trade_time`        TIME         NOT NULL                COMMENT '交易时间',
    `price`             DECIMAL(12,3) DEFAULT NULL           COMMENT '当前价',
    `volume`            BIGINT       DEFAULT NULL            COMMENT '累计成交量',
    `amount`            DECIMAL(18,2) DEFAULT NULL           COMMENT '累计成交额',
    `avg_price`         DECIMAL(12,3) DEFAULT NULL           COMMENT '均价',
    PRIMARY KEY (`id`),
    KEY `idx_stock_date` (`stock_code`, `trade_date`),
    KEY `idx_stock_date_time` (`stock_code`, `trade_date`, `trade_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股票分时交易数据表';


-- ============================================
-- 6. 行业分类表 (stock_industry)
-- ============================================
CREATE TABLE IF NOT EXISTS `stock_industry` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `industry_code`     VARCHAR(32)  NOT NULL                COMMENT '行业编码',
    `industry_name`     VARCHAR(64)  NOT NULL                COMMENT '行业名称',
    `classification`    VARCHAR(32)  NOT NULL                COMMENT '分类体系：SWS-申万, CSRC-证监会',
    `parent_code`       VARCHAR(32)  DEFAULT NULL            COMMENT '上级行业编码',
    `level`             TINYINT      DEFAULT 1               COMMENT '层级：1-一级, 2-二级, 3-三级',
    `create_id`         BIGINT       DEFAULT NULL            COMMENT '创建者ID',
    `create_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`         BIGINT       DEFAULT NULL            COMMENT '更新者ID',
    `update_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_industry` (`classification`, `industry_code`),
    KEY `idx_parent_code` (`parent_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行业分类表';


-- ============================================
-- 7. 股东信息表 (stock_shareholder)
-- ============================================
CREATE TABLE IF NOT EXISTS `stock_shareholder` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `stock_code`        VARCHAR(16)  NOT NULL                COMMENT '股票代码',
    `holder_name`       VARCHAR(128) NOT NULL                COMMENT '股东名称',
    `holder_type`       VARCHAR(16)  NOT NULL                COMMENT '类型：PERSON-个人, INSTITUTION-机构, FUND-基金, OVERSEAS-外资',
    `hold_shares`       BIGINT       DEFAULT NULL            COMMENT '持股数（股）',
    `hold_pct`          DECIMAL(10,4) DEFAULT NULL           COMMENT '持股比例（%）',
    `change_shares`     BIGINT       DEFAULT NULL            COMMENT '较上期变动（股）',
    `report_date`       DATE         NOT NULL                COMMENT '报告期',
    `create_id`         BIGINT       DEFAULT NULL            COMMENT '创建者ID',
    `create_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`         BIGINT       DEFAULT NULL            COMMENT '更新者ID',
    `update_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_stock_code` (`stock_code`),
    KEY `idx_report_date` (`report_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股东信息表';


-- ============================================
-- 8. 基金基本信息表 (fund_info)
-- ============================================
CREATE TABLE IF NOT EXISTS `fund_info` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `fund_code`         VARCHAR(16)  NOT NULL                COMMENT '基金代码',
    `fund_name`         VARCHAR(128) NOT NULL                COMMENT '基金名称',
    `fund_type`         VARCHAR(32)  DEFAULT NULL            COMMENT '类型：STOCK-股票型, MIXED-混合型, INDEX-指数型, BOND-债券型, MONEY-货币型',
    `manager`           VARCHAR(64)  DEFAULT NULL            COMMENT '基金经理',
    `company`           VARCHAR(128) DEFAULT NULL            COMMENT '基金公司',
    `nav`               DECIMAL(10,4) DEFAULT NULL           COMMENT '最新净值',
    `total_share`       DECIMAL(18,2) DEFAULT NULL          COMMENT '总份额（万份）',
    `establish_date`    DATE         DEFAULT NULL            COMMENT '成立日期',
    `create_id`         BIGINT       DEFAULT NULL            COMMENT '创建者ID',
    `create_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`         BIGINT       DEFAULT NULL            COMMENT '更新者ID',
    `update_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_fund_code` (`fund_code`),
    KEY `idx_fund_type` (`fund_type`),
    KEY `idx_company` (`company`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基金基本信息表';


-- ============================================
-- 9. 股票-基金持仓关联表 (stock_fund_rel)
-- 纯关联表，无审计字段
-- ============================================
CREATE TABLE IF NOT EXISTS `stock_fund_rel` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `stock_code`        VARCHAR(16)  NOT NULL                COMMENT '股票代码',
    `fund_code`         VARCHAR(16)  NOT NULL                COMMENT '基金代码',
    `hold_shares`       BIGINT       DEFAULT NULL            COMMENT '持仓股数',
    `hold_pct`          DECIMAL(10,4) DEFAULT NULL           COMMENT '占净值比例（%）',
    `report_date`       DATE         NOT NULL                COMMENT '报告期',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stock_fund_report` (`stock_code`, `fund_code`, `report_date`),
    KEY `idx_fund_code` (`fund_code`),
    KEY `idx_report_date` (`report_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股票-基金持仓关联表';


-- ============================================
-- 10. 年报/财报信息表 (stock_annual_report)
-- ============================================
CREATE TABLE IF NOT EXISTS `stock_annual_report` (
    `id`                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `stock_code`         VARCHAR(16)  NOT NULL                COMMENT '股票代码',
    `report_year`        INT          NOT NULL                COMMENT '报告年度',
    `report_type`        VARCHAR(8)   NOT NULL                COMMENT '报告类型：ANNUAL-年报, SEMI-半年报, Q1-一季报, Q3-三季报',
    `revenue`            DECIMAL(20,2) DEFAULT NULL           COMMENT '营业总收入（元）',
    `revenue_yoy`        DECIMAL(10,4) DEFAULT NULL           COMMENT '营收同比（%）',
    `net_profit`         DECIMAL(20,2) DEFAULT NULL           COMMENT '归母净利润（元）',
    `net_profit_yoy`     DECIMAL(10,4) DEFAULT NULL           COMMENT '净利润同比（%）',
    `deducted_profit`    DECIMAL(20,2) DEFAULT NULL           COMMENT '扣非净利润（元）',
    `eps`                DECIMAL(10,4) DEFAULT NULL           COMMENT '基本每股收益',
    `bvps`               DECIMAL(10,4) DEFAULT NULL           COMMENT '每股净资产',
    `roe`                DECIMAL(10,4) DEFAULT NULL           COMMENT '净资产收益率（%）',
    `roa`                DECIMAL(10,4) DEFAULT NULL           COMMENT '总资产收益率（%）',
    `gross_margin`       DECIMAL(10,4) DEFAULT NULL           COMMENT '销售毛利率（%）',
    `net_margin`         DECIMAL(10,4) DEFAULT NULL           COMMENT '销售净利率（%）',
    `total_assets`       DECIMAL(20,2) DEFAULT NULL           COMMENT '总资产（元）',
    `total_liabilities`  DECIMAL(20,2) DEFAULT NULL           COMMENT '总负债（元）',
    `debt_ratio`         DECIMAL(10,4) DEFAULT NULL           COMMENT '资产负债率（%）',
    `create_id`          BIGINT       DEFAULT NULL            COMMENT '创建者ID',
    `create_time`        DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`          BIGINT       DEFAULT NULL            COMMENT '更新者ID',
    `update_time`        DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stock_report` (`stock_code`, `report_year`, `report_type`),
    KEY `idx_report_year` (`report_year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股票年报/财报信息表';


-- ============================================
-- 11. 股票动态事件表 (stock_event)
-- 记录股东增减持、指数调入调出、ST标记、退市、分红等事件
-- ============================================
CREATE TABLE IF NOT EXISTS `stock_event` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `stock_code`        VARCHAR(16)  NOT NULL                COMMENT '股票代码',
    `event_type`        VARCHAR(32)  NOT NULL                COMMENT '事件类型：SHAREHOLDER_ADD-增持,SHAREHOLDER_REDUCE-减持,INDEX_IN-调入指数,INDEX_OUT-调出指数,ST_IN-列入ST,ST_OUT-撤销ST,DELIST-退市,RELIST-重新上市,DIVIDEND-分红,RIGHTS_ISSUE-配股,NAME_CHANGE-更名,OTHER-其他',
    `event_date`        DATE         NOT NULL                COMMENT '事件日期',
    `announce_date`     DATE         DEFAULT NULL            COMMENT '公告日期',
    `event_title`       VARCHAR(256) DEFAULT NULL            COMMENT '事件标题',
    `event_content`     TEXT         DEFAULT NULL            COMMENT '事件详情',
    `ext_data`          JSON         DEFAULT NULL            COMMENT '扩展数据，按事件类型自定义：增减持{\"change_amount\":-180000,\"change_pct\":-0.5,\"related_party\":\"xxx\"}；指数调入{\"index_name\":\"上证50\"}；ST{\"reason\":\"连亏两年\"}',
    `source`            VARCHAR(128) DEFAULT NULL            COMMENT '数据来源',
    `create_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_stock_code` (`stock_code`),
    KEY `idx_event_type` (`event_type`),
    KEY `idx_event_date` (`event_date`),
    KEY `idx_stock_event` (`stock_code`, `event_type`, `event_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股票动态事件表';


-- ============================================
-- 12. 选股策略执行记录表 (stock_strategy)
-- 每次策略执行一条记录，关联表存命中股票
-- ============================================
CREATE TABLE IF NOT EXISTS `stock_strategy` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `strategy_code`     VARCHAR(64)  NOT NULL                COMMENT '策略编码',
    `strategy_name`     VARCHAR(128) NOT NULL                COMMENT '策略名称',
    `strategy_desc`     VARCHAR(512) DEFAULT NULL            COMMENT '策略描述',
    `category`          VARCHAR(32)  DEFAULT NULL            COMMENT '分类：TREND-趋势,MOMENTUM-动量,VOLUME-量价,PATTERN-形态,BREAK-突破',
    `exec_date`         DATE         NOT NULL                COMMENT '策略执行日期',
    `params`            JSON         DEFAULT NULL            COMMENT '策略参数，如{\"vol_days\":3,\"rise_pct\":5,\"drop_days\":2}',
    `status`            TINYINT      DEFAULT 1               COMMENT '状态：1-启用，0-禁用',
    `create_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_exec_date` (`exec_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选股策略执行记录表';


-- ============================================
-- 13. 选股策略关联表 (stock_strategy_rel)
-- 记录策略执行后命中了哪些股票
-- ============================================
CREATE TABLE IF NOT EXISTS `stock_strategy_rel` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `strategy_id`       BIGINT       NOT NULL                COMMENT '策略ID',
    `stock_code`        VARCHAR(16)  NOT NULL                COMMENT '股票代码',
    `create_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_strategy_stock` (`strategy_id`, `stock_code`),
    KEY `idx_stock_code` (`stock_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选股策略关联表';


-- ============================================
-- 14. 估值指标表 (stock_valuation)
-- PE/PB/PS/市值/股息率/历史分位，按交易日存储
-- ============================================
CREATE TABLE IF NOT EXISTS `stock_valuation` (
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `stock_code`          VARCHAR(16)  NOT NULL                COMMENT '股票代码',
    `trade_date`          DATE         NOT NULL                COMMENT '交易日',
    `pe_ttm`              DECIMAL(10,2) DEFAULT NULL           COMMENT '市盈率(TTM)',
    `pe_static`           DECIMAL(10,2) DEFAULT NULL           COMMENT '市盈率(静态)',
    `pe_dynamic`          DECIMAL(10,2) DEFAULT NULL           COMMENT '市盈率(动态)',
    `pb`                  DECIMAL(10,2) DEFAULT NULL           COMMENT '市净率',
    `ps`                  DECIMAL(10,2) DEFAULT NULL           COMMENT '市销率',
    `total_mv`            DECIMAL(16,2) DEFAULT NULL           COMMENT '总市值(亿)',
    `circulating_mv`      DECIMAL(16,2) DEFAULT NULL           COMMENT '流通市值(亿)',
    `div_yield`           DECIMAL(10,4) DEFAULT NULL           COMMENT '股息率(%)',
    `pe_percentile`       DECIMAL(10,4) DEFAULT NULL           COMMENT 'PE历史分位(%)',
    `pb_percentile`       DECIMAL(10,4) DEFAULT NULL           COMMENT 'PB历史分位(%)',
    `create_time`         DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stock_date` (`stock_code`, `trade_date`),
    KEY `idx_trade_date` (`trade_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='估值指标表';


-- ============================================
-- 15. 公司产品/业务表 (stock_product)
-- ============================================
CREATE TABLE IF NOT EXISTS `stock_product` (
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `stock_code`          VARCHAR(16)  NOT NULL                COMMENT '股票代码',
    `product_name`        VARCHAR(128) NOT NULL                COMMENT '产品/业务名称',
    `product_type`        VARCHAR(32)  NOT NULL                COMMENT '类型：MAIN-主营,NEW-新业务,OTHER-其他',
    `revenue_ratio`       DECIMAL(10,4) DEFAULT NULL           COMMENT '营收占比(%)',
    `gross_margin`        DECIMAL(10,4) DEFAULT NULL           COMMENT '毛利率(%)',
    `description`         VARCHAR(512) DEFAULT NULL            COMMENT '产品描述',
    `report_year`         INT          DEFAULT NULL            COMMENT '报告年份',
    `status`              TINYINT      DEFAULT 1               COMMENT '状态：1-启用，0-禁用',
    `create_time`         DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stock_product` (`stock_code`, `product_name`, `report_year`),
    KEY `idx_stock_code` (`stock_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公司产品/业务表';


-- ============================================
-- 16. 行业产业链表 (stock_industry_chain)
-- ============================================
CREATE TABLE IF NOT EXISTS `stock_industry_chain` (
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `stock_code`          VARCHAR(16)  NOT NULL                COMMENT '股票代码',
    `relation_type`       VARCHAR(16)  NOT NULL                COMMENT '关系：UPSTREAM-上游,DOWNSTREAM-下游,COMPETITOR-同行,SUBSTITUTE-替代',
    `related_stock_code`  VARCHAR(16)  DEFAULT NULL            COMMENT '关联股票代码',
    `related_company`     VARCHAR(128) NOT NULL                COMMENT '关联公司名称',
    `relation_desc`       VARCHAR(512) DEFAULT NULL            COMMENT '关系描述',
    `status`              TINYINT      DEFAULT 1               COMMENT '状态：1-启用，0-禁用',
    `create_time`         DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stock_relation` (`stock_code`, `relation_type`, `related_company`),
    KEY `idx_stock_code` (`stock_code`),
    KEY `idx_related_stock` (`related_stock_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行业产业链表';
