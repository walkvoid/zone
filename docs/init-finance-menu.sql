-- ============================================
-- Zone 金融管理菜单初始化
-- ============================================

-- 顶级目录：金融管理
INSERT INTO menu (id, parent_id, menu_code, menu_name, url, menu_type, icon, sort, permission, visible, status, create_id, update_id, create_time, update_time) VALUES
(500, 0, 'FinanceManage', '金融管理', '/finance', '0', 'ant-design:fund-outlined', 60, NULL, 1, 1, 1, 1, NOW(), NOW());

-- 子菜单
INSERT INTO menu (id, parent_id, menu_code, menu_name, url, menu_type, icon, sort, permission, visible, status, create_id, update_id, create_time, update_time) VALUES
(501, 500, 'StockIndicator', '股票指标', '/finance/stock-indicator', '1', 'ant-design:rise-outlined', 1, 'Finance:StockIndicator:List', 1, 1, 1, 1, NOW(), NOW()),
(502, 500, 'FundIndicator', '基金指标', '/finance/fund-indicator', '1', 'ant-design:account-book-outlined', 2, 'Finance:FundIndicator:List', 1, 1, 1, 1, NOW(), NOW()),
(503, 500, 'MacroIndicator', '宏观指标', '/finance/macro-indicator', '1', 'ant-design:global-outlined', 3, 'Finance:MacroIndicator:List', 1, 1, 1, 1, NOW(), NOW());
